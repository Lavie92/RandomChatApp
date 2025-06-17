import os
import re
import openai
from github import Github

# Retrieve environment variables
openai_api_key = os.getenv("OPENAI_API_KEY")
github_token = os.getenv("GITHUB_TOKEN")
repo_name = os.getenv("GITHUB_REPOSITORY")
pr_number_str = os.getenv("PR_NUMBER")

# Basic validation of required environment variables
if not openai_api_key or not github_token or not repo_name or not pr_number_str:
    raise RuntimeError("Missing one or more required environment variables: OPENAI_API_KEY, GITHUB_TOKEN, GITHUB_REPOSITORY, PR_NUMBER")

pr_number = int(pr_number_str)

# Set up OpenAI API key and GitHub client
openai.api_key = openai_api_key
github_client = Github(github_token)
repo = github_client.get_repo(repo_name)
pr = repo.get_pull(pr_number)

# Get the latest commit object for the PR (to anchor comments)
commit = repo.get_commit(pr.head.sha)

# Prepare a list to record comments for output
output_lines = []

# Iterate over each changed file in the pull request
for file in pr.get_files():
    # Only review Kotlin files with available diff patch
    if not file.filename.endswith(".kt") or not file.patch:
        continue

    patch_text = file.patch
    patch_lines = patch_text.splitlines()
    new_line_num = None  # Will hold the current line number in the new file as we parse

    # Parse the unified diff patch line by line
    for line in patch_lines:
        # Identify the start of a diff hunk and set the starting line number for the new file
        if line.startswith('@@'):
            match = re.match(r'.*\+(\d+)(?:,(\d+))?', line)
            if match:
                new_line_num = int(match.group(1))
            else:
                new_line_num = None
            continue

        # Skip processing if we're not in a diff hunk yet
        if new_line_num is None:
            continue

        # Skip diff metadata lines for file paths
        if line.startswith('+++') or line.startswith('---'):
            continue

        # Added line in new file
        if line.startswith('+'):
            # Ignore the "+++" filename line (already handled above) and empty added lines
            if line.startswith('+++'):
                continue
            content = line[1:]
            if content.strip() == "":
                # If the added line is blank or only whitespace, skip commenting but still increment line number
                new_line_num += 1
                continue

            # Construct the prompt for GPT-3.5 to review this line
            prompt = "Please review the following Kotlin line and comment if it contains any bug, code smell, or improvement opportunity. Only respond if there is a valid suggestion:\n\n" + content

            # Call the OpenAI ChatCompletion API for the line
            try:
                response = openai.ChatCompletion.create(
                    model="gpt-3.5-turbo",
                    messages=[{"role": "user", "content": prompt}],
                    temperature=0.3
                )
                message = response.choices[0].message.content.strip()
            except Exception as e:
                print(f"OpenAI API call failed for {file.filename} (line {new_line_num}): {e}")
                # Skip commenting this line if API call fails
                new_line_num += 1
                continue

            # If GPT returned a suggestion (and not an empty or "No issue" response), post it as a review comment
            if message and not re.match(r'^\s*(?:no issue?s?(?: found)?)[\.]?$', message, flags=re.IGNORECASE):
                try:
                    pr.create_review_comment(
                        body=message,
                        commit=commit,
                        path=file.filename,
                        line=new_line_num,
                        side="RIGHT"
                    )
                    output_lines.append(f"{file.filename} (line {new_line_num}): {message}")
                except Exception as e:
                    print(f"Failed to create comment for {file.filename} (line {new_line_num}): {e}")

            # Increment the new file line number after processing an added line
            new_line_num += 1

        # Removed line in old file (not present in new file)
        elif line.startswith('-'):
            # Do not increment new_line_num for removed lines
            continue

        # Unchanged context line in new file
        else:
            new_line_num += 1

# Write the results to output.txt for artifact upload
with open("output.txt", "w") as f:
    if output_lines:
        f.write("AI Review Comments:\n")
        for out_line in output_lines:
            f.write(out_line + "\n")
    else:
        f.write("No issues found by AI.")
