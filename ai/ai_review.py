import os
import re
from github import Github
from openai import OpenAI

openai_api_key = os.getenv("OPENAI_API_KEY")
github_token = os.getenv("GITHUB_TOKEN")
repo_name = os.getenv("GITHUB_REPOSITORY")
pr_number_str = os.getenv("PR_NUMBER")

if not openai_api_key or not github_token or not repo_name or not pr_number_str:
    raise RuntimeError("Missing one or more required environment variables: OPENAI_API_KEY, GITHUB_TOKEN, GITHUB_REPOSITORY, PR_NUMBER")

pr_number = int(pr_number_str)

ai = OpenAI(api_key=openai_api_key)
github_client = Github(github_token)
repo = github_client.get_repo(repo_name)
pr = repo.get_pull(pr_number)
commit = repo.get_commit(pr.head.sha)

output_lines = []

for file in pr.get_files():
    if not file.filename.endswith(".kt") or not file.patch:
        continue

    patch_text = file.patch
    patch_lines = patch_text.splitlines()
    new_line_num = None

    for line in patch_lines:
        if line.startswith('@@'):
            match = re.match(r'.*\+(\d+)(?:,(\d+))?', line)
            if match:
                new_line_num = int(match.group(1))
            else:
                new_line_num = None
            continue

        if new_line_num is None:
            continue

        if line.startswith('+++') or line.startswith('---'):
            continue

        if line.startswith('+'):
            if line.startswith('+++'):
                continue
            content = line[1:]
            if content.strip() == "":
                new_line_num += 1
                continue

            prompt = "Please review the following Kotlin line and comment if it contains any bug, code smell, or improvement opportunity. Only respond if there is a valid suggestion:\n\n" + content

            try:
                response = ai.chat.completions.create(
                    model="gpt-3.5-turbo",
                    messages=[{"role": "user", "content": prompt}],
                    temperature=0.3
                )
                message = response.choices[0].message.content.strip()
            except Exception as e:
                print(f"OpenAI API call failed for {file.filename} (line {new_line_num}): {e}")
                new_line_num += 1
                continue

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

            new_line_num += 1

        elif line.startswith('-'):
            continue

        else:
            new_line_num += 1

with open("output.txt", "w") as f:
    if output_lines:
        f.write("AI Review Comments:\n")
        for out_line in output_lines:
            f.write(out_line + "\n")
    else:
        f.write("No issues found by AI.")
