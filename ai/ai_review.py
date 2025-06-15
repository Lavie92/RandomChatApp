import os
import openai
from github import Github
from openai import OpenAI
from openai.types.chat import ChatCompletionMessageParam

openai_api_key = os.getenv("OPENAI_API_KEY")
github_token = os.getenv("GITHUB_TOKEN")
repo_name = os.getenv("GITHUB_REPOSITORY")
pr_number = int(os.getenv("PR_NUMBER"))
model = os.getenv("OPENAI_MODEL", "gpt-3.5-turbo")

ai = OpenAI(api_key=openai_api_key)
g = Github(github_token)
repo = g.get_repo(repo_name)
pr = repo.get_pull(pr_number)

review_comment = ""

for file in pr.get_files():
    if file.filename.endswith('.kt') and file.patch:
        try:
            diff = file.patch
            prompt = f"Please review the following Kotlin code diff and provide suggestions, identify any bugs or code smells:\n\n{diff}"
            messages: list[ChatCompletionMessageParam] = [
                {"role": "user", "content": prompt}
            ]
            chat = ai.chat.completions.create(
                model=model,
                messages=messages,
                temperature=0.2
            )
            content = chat.choices[0].message.content
            review_comment += f"**File: {file.filename}**\n{content}\n\n"
        except Exception as e:
            review_comment += f"**File: {file.filename}**\nOpenAI error: {str(e)}\n\n"

if review_comment:
    try:
        pr.create_issue_comment(review_comment)
    except Exception as e:
        print(f"Failed to post comment: {e}")
    with open("output.txt", "w", encoding="utf-8") as f:
        f.write(review_comment)
else:
    with open("output.txt", "w") as f:
        f.write("No Kotlin files changed or no reviewable diffs.")
