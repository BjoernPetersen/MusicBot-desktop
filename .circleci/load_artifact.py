import argparse
import json
import os

import requests


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


class Circle:
    _baseUrl = "https://circleci.com/api/v2"

    def __init__(self, token):
        self._token = token
        self._headers = {
            'Circle-Token': token
        }

    def get_pipelines(self, slug, branch):
        url = f"{self._baseUrl}/project/{slug}/pipeline?branch={branch}"
        response = requests.get(url, headers=self._headers)
        return response.json()['items']

    def get_workflows(self, pipeline_id):
        url = f"{self._baseUrl}/pipeline/{pipeline_id}/workflow"
        response = requests.get(url, headers=self._headers)
        return response.json()['items']

    def get_jobs(self, workflow_id):
        url = f"{self._baseUrl}/workflow/{workflow_id}/job"
        response = requests.get(url, headers=self._headers)
        return response.json()['items']

    def get_artifacts(self, slug, build_num):
        url = f"{self._baseUrl}/project/{slug}/{build_num}/artifacts"
        response = requests.get(url, headers=self._headers)
        return response.json()['items']


class Params:
    def __init__(self, args):
        self.user = check_not_none(args.user, "User must not be empty")
        self.project = check_not_none(
            args.project, "Project needs to be specified")
        self.branch = check_not_none(args.branch, "Branch must not be empty")
        self.token = check_not_none(
            get_token(args.token), "Token must be specified")

    def __repr__(self) -> str:
        return "[user=" + self.user \
               + ", project=" + self.project \
               + ", branch=" + self.branch + "]"

    def __str__(self) -> str:
        return self.__repr__()


def create_slug(user, project) -> str:
    return f"github/{user}/{project}"


def find_most_recent_workflow(client, slug, branch) -> str:
    for pipeline in client.get_pipelines(slug, branch):
        workflow = client.get_workflows(pipeline['id'])[0]
        if workflow['status'] == "success":
            print(f"Found successful pipeline: {pipeline['number']}")
            return workflow['id']
    print("Could not find successful workflow")
    exit(1)


def find_jobs(client, workflow_id):
    return client.get_jobs(workflow_id)


def download(url, output):
    with open(output, 'wb') as f:
        response = requests.get(url)
        f.write(response.content)


def load_artifacts(client: Circle, slug, build_num):
    arts = client.get_artifacts(slug, build_num)
    for artifact in arts:
        path = artifact['path']
        base_name = os.path.basename(path)
        print(f"Downloading {base_name}")
        download(artifact['url'], base_name)


def run(params: Params):
    user = params.user
    project = params.project
    branch = params.branch

    client = Circle(params.token)
    slug = create_slug(user, project)
    workflow_id = find_most_recent_workflow(client, slug, branch)
    jobs = find_jobs(client, workflow_id)

    for job in jobs:
        if job['name'].startswith("build_"):
            load_artifacts(client, slug, job['job_number'])


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
    parser.add_argument("--token", "-t",
                        dest="token",
                        action="store",
                        help="An API token (currently personal access token). Can be left empty and"
                             " set as CIRCLE_TOKEN environment variable instead")
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
