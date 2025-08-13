#!/usr/bin/env python3
"""
GSSoC Issue Tracker Updater

This script fetches GSSoC-related issues from GitHub and updates the README.md
with the current status of issues categorized by difficulty level.

Usage:
    python scripts/update-gssoc-issues.py

Requirements:
    - requests library (pip install requests)
    - GitHub API access (optional: set GITHUB_TOKEN environment variable)
"""

import json
import os
import re
import requests
from datetime import datetime
from typing import List, Dict, Any

# Configuration
REPO_OWNER = "rookiecoder910"
REPO_NAME = "pixelpholio"
README_PATH = "Readme.md"
GITHUB_API_BASE = "https://api.github.com"

# Label mappings for difficulty levels
DIFFICULTY_LABELS = {
    "beginner": ["good-first-issue", "beginner-friendly", "easy"],
    "intermediate": ["intermediate", "medium"],
    "advanced": ["advanced", "hard", "difficult"]
}

GSSOC_LABELS = ["gssoc", "gssoc24", "girl-script"]

def get_github_headers() -> Dict[str, str]:
    """Get headers for GitHub API requests"""
    headers = {
        "Accept": "application/vnd.github.v3+json",
        "User-Agent": "GSSoC-Issue-Tracker"
    }
    
    # Add auth token if available
    github_token = os.environ.get("GITHUB_TOKEN")
    if github_token:
        headers["Authorization"] = f"token {github_token}"
    
    return headers

def fetch_gssoc_issues() -> List[Dict[str, Any]]:
    """Fetch all GSSoC-related issues from GitHub"""
    issues = []
    
    # Construct search query for GSSoC issues
    labels_query = " OR ".join([f'label:"{label}"' for label in GSSOC_LABELS])
    query = f"repo:{REPO_OWNER}/{REPO_NAME} is:issue ({labels_query})"
    
    url = f"{GITHUB_API_BASE}/search/issues"
    params = {
        "q": query,
        "sort": "created",
        "order": "desc",
        "per_page": 100
    }
    
    try:
        response = requests.get(url, headers=get_github_headers(), params=params)
        response.raise_for_status()
        
        data = response.json()
        issues = data.get("items", [])
        
        print(f"Found {len(issues)} GSSoC issues")
        return issues
        
    except requests.exceptions.RequestException as e:
        print(f"Error fetching issues: {e}")
        return []

def categorize_issues(issues: List[Dict[str, Any]]) -> Dict[str, List[Dict[str, Any]]]:
    """Categorize issues by difficulty level"""
    categorized = {
        "beginner": [],
        "intermediate": [],
        "advanced": []
    }
    
    for issue in issues:
        issue_labels = [label["name"].lower() for label in issue.get("labels", [])]
        
        # Determine difficulty level
        difficulty = "intermediate"  # default
        
        for level, level_labels in DIFFICULTY_LABELS.items():
            if any(label in issue_labels for label in level_labels):
                difficulty = level
                break
        
        categorized[difficulty].append({
            "title": issue["title"],
            "number": issue["number"],
            "url": issue["html_url"],
            "state": issue["state"],
            "labels": issue_labels,
            "assignee": issue.get("assignee", {}).get("login") if issue.get("assignee") else None,
            "created_at": issue["created_at"]
        })
    
    return categorized

def format_issue_list(issues: List[Dict[str, Any]]) -> str:
    """Format issues as markdown list"""
    if not issues:
        return "*No issues currently available. Check back soon!*"
    
    formatted_issues = []
    for issue in issues:
        status_emoji = "✅" if issue["state"] == "closed" else "🔓"
        assignee_text = f" (Assigned to @{issue['assignee']})" if issue["assignee"] else ""
        
        formatted_issues.append(
            f"- {status_emoji} [#{issue['number']} - {issue['title']}]({issue['url']}){assignee_text}"
        )
    
    return "\n".join(formatted_issues)

def calculate_statistics(categorized_issues: Dict[str, List[Dict[str, Any]]]) -> Dict[str, int]:
    """Calculate GSSoC statistics"""
    total_issues = sum(len(issues) for issues in categorized_issues.values())
    completed_issues = sum(
        len([issue for issue in issues if issue["state"] == "closed"])
        for issues in categorized_issues.values()
    )
    open_issues = total_issues - completed_issues
    
    # Count unique contributors (assignees)
    contributors = set()
    for issues in categorized_issues.values():
        for issue in issues:
            if issue["assignee"]:
                contributors.add(issue["assignee"])
    
    return {
        "total": total_issues,
        "completed": completed_issues,
        "open": open_issues,
        "contributors": len(contributors)
    }

def update_readme(categorized_issues: Dict[str, List[Dict[str, Any]]]) -> bool:
    """Update the README.md file with current GSSoC issues"""
    try:
        with open(README_PATH, "r", encoding="utf-8") as f:
            content = f.read()
        
        # Calculate statistics
        stats = calculate_statistics(categorized_issues)
        
        # Update beginner issues section
        beginner_pattern = r"(#### 🟢 Beginner-Friendly Issues\n<!-- Issues suitable for first-time contributors -->\n).*?(?=\n#### |\n### |\n---|\Z)"
        beginner_replacement = f"\\1{format_issue_list(categorized_issues['beginner'])}\n"
        content = re.sub(beginner_pattern, beginner_replacement, content, flags=re.DOTALL)
        
        # Update intermediate issues section
        intermediate_pattern = r"(#### 🟡 Intermediate Issues\s*\n<!-- Issues requiring some Android/Kotlin experience -->\n).*?(?=\n#### |\n### |\n---|\Z)"
        intermediate_replacement = f"\\1{format_issue_list(categorized_issues['intermediate'])}\n"
        content = re.sub(intermediate_pattern, intermediate_replacement, content, flags=re.DOTALL)
        
        # Update advanced issues section
        advanced_pattern = r"(#### 🔴 Advanced Issues\n<!-- Complex issues for experienced contributors -->\n).*?(?=\n#### |\n### |\n---|\Z)"
        advanced_replacement = f"\\1{format_issue_list(categorized_issues['advanced'])}\n"
        content = re.sub(advanced_pattern, advanced_replacement, content, flags=re.DOTALL)
        
        # Update statistics
        stats_pattern = r"(### 📊 GSSoC Statistics\n\n)- \*\*Total GSSoC Issues\*\*: \d+\n- \*\*Completed Issues\*\*: \d+\n- \*\*Active Contributors\*\*: \d+\n- \*\*Open Issues\*\*: \d+"
        stats_replacement = f"\\1- **Total GSSoC Issues**: {stats['total']}\n- **Completed Issues**: {stats['completed']}\n- **Active Contributors**: {stats['contributors']}\n- **Open Issues**: {stats['open']}"
        content = re.sub(stats_pattern, stats_replacement, content)
        
        # Write updated content back to file
        with open(README_PATH, "w", encoding="utf-8") as f:
            f.write(content)
        
        print(f"README.md updated successfully!")
        print(f"Statistics: {stats['total']} total, {stats['open']} open, {stats['completed']} completed")
        return True
        
    except Exception as e:
        print(f"Error updating README.md: {e}")
        return False

def main():
    """Main function"""
    print("🌟 GSSoC Issue Tracker Updater")
    print("=" * 40)
    
    # Fetch GSSoC issues
    print("Fetching GSSoC issues from GitHub...")
    issues = fetch_gssoc_issues()
    
    if not issues:
        print("No GSSoC issues found or error occurred.")
        return
    
    # Categorize issues
    print("Categorizing issues by difficulty...")
    categorized = categorize_issues(issues)
    
    # Display summary
    print("\n📊 Issue Summary:")
    for level, issue_list in categorized.items():
        print(f"  {level.title()}: {len(issue_list)} issues")
    
    # Update README
    print("\nUpdating README.md...")
    success = update_readme(categorized)
    
    if success:
        print("✅ GSSoC issue tracking completed successfully!")
    else:
        print("❌ Failed to update README.md")

if __name__ == "__main__":
    main()