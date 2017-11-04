# Maya

Welcome to the Maya project!

Working in the operations department of a company I've noticed how much time is spent connecting to remote machines to 
obtain answers for simple questions. You're working on something when you get a message from someone asking about free
disk space or the number of events received in the last week or what version is deployed of a certain component, so you
stop what you're doing, connect to the machine, execute the same set of commands and paste it into the chat. What if all
of this could be done automatically?

This is where Maya comes in with it's Skill Definition Language (SDL for short). Just define the regex it should look for,
the process it should follow and then start using it. No restarts, no editing config files, just send it a message
teaching it the skill and it'll learn it.

## Maya SDL



### Examples

- Fetch data from a rest api and write it to a file on a remote machine:
```
skill:
learn {{core.in(write (.+) in (.+)) -> rest.get($0) -> ssh.execute(pi, echo '$res0' > test/$user_$1.txt)}}
command:
write http://www.google.com in foo
```

## Modules

Currently, the following modules and methods are available when defining new skills:

- `core`

- `rest`

- `scheduler`

- `ssh`

## Contribution policy

Contributions via GitHub pull requests are gladly accepted from their original author. Along with
any pull requests, please state that the contribution is your original work and that you license
the work to the project under the project's open source license. Whether or not you state this
explicitly, by submitting any copyrighted material via pull request, email, or other means you
agree to license the material under the project's open source license and warrant that you have the
legal authority to do so.


