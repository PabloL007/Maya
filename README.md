# Maya

Welcome to the Maya project!

Working in the operations department of a company I've noticed how much time is spent connecting to remote machines to 
obtain answers for simple questions. You're working on something when you get a message from someone asking about free
disk space or the number of events received in the last week or what version is deployed of a certain component. So you
stop what you're doing, connect to the machine, execute the same set of commands and paste it into the chat. What if all
of this could be done automatically?

This is where Maya comes in with it's Skill Definition Language (MSDL for short). Just define the regex it should look for,
the process it should follow and then start using it. No restarts, no editing config files, just send it a message
teaching it the skill and it'll learn it.

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
    - `get(Complete url)`: Will return the body of the page as a String.

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

## Usage

### Configuration



### API

As this is still a work in progress there are currently no integrations implemented for chat apps, so to interact with
maya you will need a tool like postman or curl. Just send a POST request to the `/api/operations` endpoint with the 
message in the body.

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


