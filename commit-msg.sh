#!/usr/bin/env bash

# regex to validate in commit msg
commit_regex='(jira\.diamond\.ac\.uk|merge)'
error_msg="Aborting commit. Your commit message must reference a jira ticket or be 'Merge'"

last_msg = $(git log -1 --pretty=%B)

# We check the last commit message has a ticket
if [[ "$last_msg" =~ "$commit_regex" ]]; then
    exit 0
fi

echo "$error_msg" >&2
exit 1
