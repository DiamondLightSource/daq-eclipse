#!/usr/bin/env bash

# regex to validate in commit msg
commit_regex="(jira\.diamond\.ac\.uk|merge)"


last_msg="$(git log -1 --pretty=%B)"
echo "The last commit was: $last_msg"


# We check the last commit message has a ticket
if [[ $last_msg =~ .*(jira\.diamond\.ac\.uk).* ]]; then
    echo "Jira ticket or merge found, build may proceed"
    exit 0
fi

# Couldn't find ticket or merge, fail build
error_msg="Aborting build. Your commit message must reference a jira ticket"
echo "$error_msg" >&2

# Get the last user
id="$(git log --format="%H" -n 1)"
user="$(git show --format="%aN <%aE>" $id)"
echo "Last commit from:"
echo " $user"

exit 1
