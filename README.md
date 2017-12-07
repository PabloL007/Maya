# Maya v0.1.0

[![Build Status](https://travis-ci.org/PabloL007/Maya.svg?branch=develop)](https://travis-ci.org/PabloL007/Maya)
[![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/MayaTheBot/Lobby)

Welcome to the Maya project!

Working in the operations department of a company I've noticed how much time is spent connecting to remote machines to 
obtain answers for simple questions. Things that eventually make it into the alert system or a grafana dashboard after
a Jira ticket is created and someone has the time to do it. But what if, as a developer, you could just automate the
execution of those commands sitting in a text file on your pc using regex and your company's messaging app?

This is where Maya comes in with it's Skill Definition Language (MSDL for short). Just define the regex it should look
for, the process it should follow and then start using it by sending messages.

Currently, it can only receive messages through a rest API but integration with messaging apps like hipchat will come
shortly. 

## Maya SDL

When Maya is started for the first time it only has one skill, learning. This skill is triggered by sending it a message
that matches the following pattern:

```
learn {{ Maya SDL }}
```

As you can see it's just a wrapper for something called MSDL (Maya Skill Definition Language). This language is inspired
by the akka streams dsl in the sense that elements that resemble sources, flows and sinks are present, all of which are
represented by method calls separated by arrows. So let's analyse the MSDL for the learn skill:

```
core.in(learn \\{\\{(.+)\\}\\}) -> core.learn($0)
```

Here we can distinguish a few different elements:

- Modules: In this case only one (`core`)

- Methods: In this case two, `in` and `learn`

- Arguments: For `in` we have a regex and for `learn` we have a templating variable that represents the value of the
             first capture group of the regex.
             
- Separator: The arrow (->)

### Modules

Currently, the following modules and methods are available when defining new skills:

- `core`:
    - `learn(MSDL string)`: Will return the UUID of the new skill.

- `rest`:
    - `get(Complete url, Headers with format key:value (Optional))`: Will return the body of the page as a string.
    - `post(Complete url, Content type, Body of the request, Headers with format key:value (Optional))`: Will return the body of the response as a string.

- `scheduler`:
    - `schedule(Quartz cron expression)`: Will execute the rest of the skill every time the trigger is fired. 
                                          Expressions generated using [CronMaker](http://www.cronmaker.com/) should work.

- `ssh`:
    - `execute(Server alias, Command to execute)`: Will return the output of the command if any.

> You might be wondering why the in method for the core module is missing. This was done intentionally as it isn't an
> actual module method, it is just a consequence of how MSDL is parsed and can be substituted by any other string. In
> the future this will be replaced but for now it seems like a good way to call that imaginary method whose argument is
> very important.

### Templating variables

In MSDL there are several variables that can be included in arguments:

- `$N`: Value of the input string capture group N.
- `$resN`: Value of the Nth result of the whole operation.
- `$user`: Name or id of the user that sent the command.

### Examples

- Fetch data from a rest api and write it to a file on a remote machine:
    - skill:
    ```
    learn {{core.in(write (.+) in (.+)) -> rest.get($0) -> ssh.execute(pi, echo '$res0' > test/$user_$1.txt)}}
    ```
    - command:
    ```
    write http://www.google.com in foo
    ```
    
- Post a note to pushbullet:
    - skill:
    ```
    learn {{core.in(post note with title "(.+)" and content "(.+)") -> rest.post(https://api.pushbullet.com/v2/pushes,application/json,{"body":"$1","title":"$0","type":"note"},Access-Token:x.xxxxxxxxxxxxxxx)}}
    ```
    - command:
    ```
    post note with title "Maya post test" and content "this is a test"
    ```

## Usage

### Configuration

Some modules can be initialized with external configuration files, currently the ssh module is the only one. To provide
a list of ssh servers to be used with Maya, place a file named `ssh.json` in the root of the project dir (or map it to 
be placed inside `/opt/docker` if using the container) with the servers defined like so:

```json
[
  {
    "alias": "heartOfGold",
    "host": "192.168.1.2",
    "user": "guest"
  },
  {
    "alias": "bbbb",
    "host": "orders.bigbangburgerbar.com",
    "user": "Arthur"
  }
]
```

The password for these servers will have to be inputted upon initialization of the ssh module through the console.

### API

As this is still a work in progress there are currently no integrations implemented for chat apps, so to interact with
maya you will need a tool like postman or curl. Just send a POST request to the `/api/operations` endpoint with the 
message in the body.

### Docker

This application can be run as a docker, to create the container simply use the following command:

```bash
sbt docker:publishLocal
```
And for executing it, this will do:
```bash
docker run -it --rm -p 8080:8080 -e "HOST=0.0.0.0" maya:0.1.0
```

## Dependencies

Maya makes use of the following open source projects:

- [akka](https://github.com/akka/akka): For great concurrency
- [akka-http](https://github.com/akka/akka-http): For easy http endpoints and rest API client calls
- [jassh](https://github.com/dacr/jassh): For ssh in scala
- [json4s](https://github.com/json4s/json4s): For simple json handling
- [logback](https://github.com/qos-ch/logback): For logging
- [akka-quartz-scheduler](https://github.com/enragedginger/akka-quartz-scheduler): For using quartz with akka

## Contribution policy

Contributions via GitHub pull requests are gladly accepted from their original author. Along with
any pull requests, please state that the contribution is your original work and that you license
the work to the project under the project's open source license. Whether or not you state this
explicitly, by submitting any copyrighted material via pull request, email, or other means you
agree to license the material under the project's open source license and warrant that you have the
legal authority to do so.


