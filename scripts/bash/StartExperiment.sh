#!/usr/bin/expect -f
spawn nohup java -ea -cp /Network/Servers/maccsserver.lclark.edu/Users/sofdev/Documents/workspace/Orego/bin orego.experiment.Broadcast
expect {
	"Password:" {
		send "sofdev"
		exp_continue
	}
	eof {
	}
}

# make sure to update results directory / password when running this script!