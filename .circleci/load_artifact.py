import argparse
import json
import os

import requests
from circleclient import circleclient


def check_not_none(o, m):
    if not o or not o.strip():
        raise ValueError(m)
    else:
        return o


def get_token(token):
    if token:
        return token
    else:
        return os.environ["CIRCLE_TOKEN"]


def pretty(j):
    return json.dumps(j, sort_keys=True, indent=4)


def pretty_print(j):
    print(pretty(j))


class Params:
    def __init__(self, args):
        self.user = check_not_none(args.user, "User must not be empty")
        self.project = check_not_none(
            args.project, "Project needs to be specified")
        self.branch = check_not_none(args.branch, "Branch must not be empty")
        self.path = check_not_none(
            args.path, "Artifact path must be specified")
        self.token = check_not_none(
            get_token(args.token), "Token must be specified")

    def __repr__(self) -> str:
        return "[user=" + self.user \
               + ", project=" + self.project \
               + ", branch=" + self.branch \
               + ", path=" + self.path + "]"

    def __str__(self) -> str:
        return self.__repr__()


def find_most_recent_successful(client, user, project, branch):
    recents = client.build.recent(user, project, branch=branch)
    for build in recents:
        if build['status'] == "success":
            return build['build_num']
    return None


def download(url, output):
    with open(output, 'wb') as f:
        response = requests.get(url)
        f.write(response.content)


def load_artifact(client: circleclient.CircleClient, user, project, build_num, path):
    arts = client.build.artifacts(user, project, build_num)
    for artifact in arts:
        if artifact['path'] == path:
            download(artifact['url'], os.path.basename(path))
            return
    print("No artifact with path " + path + " found")
    exit(1)


def run(params: Params):
    user = params.user
    project = params.project
    branch = params.branch
    path = params.path

    client = circleclient.CircleClient(params.token)
    most_recent = find_most_recent_successful(client, user, project, branch)
    if most_recent:
        load_artifact(client, user, project, most_recent, path)
    else:
        print("No successful build found.")
        exit(1)


def main():
    parser = argparse.ArgumentParser(
        description="Load an artifact from CircleCI")
    parser.add_argument("--user", "-u",
                        dest="user",
                        action="store",
                        default="BjoernPetersen",
                        help="A CircleCI user/org")
    parser.add_argument("--project", "-p",
                        dest="project",
                        action="store",
                        required=True,
                        help="A CircleCI project name")
    parser.add_argument("--branch", "-b",
                        dest="branch",
                        action="store",
                        default="master",
                        help="A branch to load from")
    parser.add_argument("--artifact", "-a",
                        dest="path",
                        action="store",
                        required=True,
                        help="The artifact path")
    parser.add_argument("--token", "-t",
                        dest="token",
                        action="store",
                        help="An API token with artifact read permission. Can be left empty and set as CIRCLE_TOKEN "
                        "environment variable instead")
    args = parser.parse_args()

    try:
        params = Params(args)
    except ValueError as e:
        print("Argument error: " + str(e))
        exit(1)
        return
    run(params)
    exit(0)


if __name__ == "__main__":
    main()
