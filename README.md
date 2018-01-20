# prtweeter

This is a commandline App to poll a specified github repository for
new pull request, and tweet a summary for each one on a twitter
account.

## Installation

Clone via

    $ git clone git@github.com:bitti/pr-tweeter.git

build with [Leiningen](https://leiningen.org) via

    $ cd pr-tweeter
    $ lein uberjar

and move the resulting jar

    target/uberjar/prtweeter-0.1.0-SNAPSHOT-standalone.jar

to an appropriate location.

## Usage

You can just use the `lein run` command in the source directory. If
you prefer to use the jar you can use the `java` command

    $ java -jar prtweeter-0.1.0-SNAPSHOT-standalone.jar

On some systems like Linux you can also make the jar executable

    $ chmod +x prtweeter-0.1.0-SNAPSHOT-standalone.jar

and start it like any other executable in a terminal. (It's not
advisible to just click the jar in a graphical environment, the App is
expecting a commandline environment.)

On first start you'll be asked for necessary configuration parameters.
These are a github username and repository (which don't have to be
yours, since only github read access is needed) and OAuth credentials
for your Twitter account. To get these you need to create a "Twitter
App" at https://apps.twitter.com/app/new for your twitter account.

To avoid spamming your account by default a maximum of 5 tweets per
run is generated. You can change this default and other options by
changing the configuration as explained in the extended documentation
in [doc/configuration.md](doc/configuration.md).

## Options

If you provide the -i option you'll be asked for confirmation for each
tweet before it is send. This might be useful if you want to test
things out without the risk of spamming your twitter account.

To confirm just use enter (or type 'y' before pressing enter, which is
the default), to deny you can use 'n' and press enter.

## Example usages

If you want to run this script in regular intervals, you could install
it as a cron job, use the unix at(1) tool or just use a simple script
like

    while :
    do
        java -jar prtweeter-0.1.0-SNAPSHOT-standalone.jar
        sleep 600 # Wait 10 minutes before next run
    done

Note that while it's up to you how frequently you want to poll github,
the intervalls shouldn't probably be shorter than say 5 minutes in
order to avoid hitting API limits. Even for quite busy projects this
should be often enough though.

### Bugs

There is a theoretical chance for a race condition when two pull
requests are opened at the same time when prtweeter is running. In
this case one pull request might not get posted even on a rerun. Let
me know if you ever encounter this.

## License

Copyright © 2017 David Ongaro

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
