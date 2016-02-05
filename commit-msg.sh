#!/usr/bin/env bash

# regex to validate in commit msg
commit_regex='(jira\.diamond\.ac\.uk|merge)'
error_msg="Aborting commit. Your commit message must reference a jira ticket or be 'Merge'"

# We check the last commit message has a ticket
if ! grep -iqE "$commit_regex" "./.git/COMMIT_EDITMSG"; then
    echo "$error_msg" >&2
    exit 1
fi
