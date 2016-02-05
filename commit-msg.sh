#!/usr/bin/env bash

# Hope that this works on travis ci
# set this to your active development branch
develop_branch="master"
current_branch="$(git rev-parse --abbrev-ref HEAD)"

echo "Checking ${develop_branch}"
echo "Echo checked out branch ${current_branch}"

# only check commit messages on main development branch
[ "$current_branch" != "$develop_branch" ] && exit 0

# regex to validate in commit msg
commit_regex='(jira\.diamond\.ac\.uk|merge)'
error_msg="Aborting commit. Your commit message must reference a jira ticket or be 'Merge'"

if ! grep -iqE "$commit_regex" "$1"; then
    echo "$error_msg" >&2
    exit 1
fi
