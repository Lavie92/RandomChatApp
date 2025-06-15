import os
import openai
from github import Github

openai.api_key = os.getenv("OPENAI_API_KEY")
repo_name = os.getenv("GITHUB_REPOSITORY")
pr_number = int(os.getenv("PR_NUMBER"))
github_token = os.getenv("GITHUB_TOKEN")
model = os.getenv("OPENAI_MODEL", "gpt-4o")

g = Github(github_token)
repo = g.get_repo(repo_name)
pr = repo.get_pull(pr_number)

review_comment = ""

for file in pr.get_files():
    if file.filename.endswith('.kt'):
        diff = file.patch
        if diff:
            prompt = f"Please review the following Kotlin code diff and provide suggestions, point out bugs or code smells:\n\n{diff}"
            chat = openai.ChatCompletion.create(
                model=model,
                messages=[{"role": "user", "content": prompt}]
            )
            review_comment += f"**File: {file.filename}**\n{chat.choices[0].message.content}\n\n"

if review_comment:
    pr.create_issue_comment(review_comment)
    with open("output.txt", "w") as f:
        f.write(review_comment)
else:
    with open("output.txt", "w") as f:
        f.write("No Kotlin files changed or no diffs to review.")
