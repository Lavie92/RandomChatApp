import os
import re
from github import Github
from openai import OpenAI

openai_api_key = os.getenv("OPENAI_API_KEY")
github_token = os.getenv("GITHUB_TOKEN")
repo_name = os.getenv("GITHUB_REPOSITORY")
pr_number = int(os.getenv("PR_NUMBER"))

ai = OpenAI(api_key=openai_api_key)
g = Github(github_token)
repo = g.get_repo(repo_name)
pr = repo.get_pull(pr_number)
commit = repo.get_commit(pr.head.sha)

for file in pr.get_files():
    if not file.filename.endswith(".kt") or not file.patch:
        continue

    patch_lines = file.patch.split("\n")
    position = 0

    for i, line in enumerate(patch_lines):
        if line.startswith("+") and not line.startswith("+++"):
            code_line = line[1:].strip()
            if not code_line:
                continue

            prompt = f"Please review the following Kotlin line and comment if it contains any bug, code smell, or improvement opportunity. Only respond if there is a valid suggestion:\n\n{code_line}"
            response = ai.chat.completions.create(
                model="gpt-3.5-turbo",
                messages=[{"role": "user", "content": prompt}],
                temperature=0.3
            )
            message = response.choices[0].message.content.strip()

            if message and not message.lower().startswith("no issues"):
                try:
                    pr.create_review_comment(
                        body=message,
                        commit=commit,
                        path=file.filename,
                        position=position + 1
                    )
                except Exception as e:
                    print(f"Failed to comment on {file.filename}:{i} – {e}")
        if not line.startswith("-"):
            position += 1
