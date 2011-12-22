#!/usr/bin/perl

use strict;
use warnings;

if(!defined @ARGV || @ARGV < 5) {
	print "Usage: ./genPlayers.pl MIN_COEFF1 MAX_COEFF1 STEP1 MIN_COEFF2 MAX_COEFF2 STEP2\n";
	exit(0);
}

my $i = $ARGV[0];
for(; $i <= $ARGV[1]; $i += $ARGV[2]) {
	for(my $j = $ARGV[3]; $j <= $ARGV[4]; $j+= $ARGV[5]) {
			print "%section player\n";
			print "\tname\tOrego-beta-$i-$j\n";
			print "\tpassword\toregoreu2008\n";
			print "\tinvoke\t./engine.sh $i $j\n";
			print "\tpriority\t1\n\n";
	}
}
