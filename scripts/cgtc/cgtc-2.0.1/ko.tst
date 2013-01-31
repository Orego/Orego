#------------------------------------------------------------------------------
# Ko
#
# Computer Go Test Collection http://www.cs.ualberta.ca/~games/go/
#
# $Source: /Users/sofdev/cvsroot/Orego/scripts/cgtc/cgtc-2.0.1/ko.tst,v $
# $Id: ko.tst,v 1.1 2009/06/17 21:59:04 sofdev Exp $
#------------------------------------------------------------------------------

loadsgf ./sgf/ko/ko.1.sgf

10 reg_genmove white
#? [M5]

20 reg_genmove white
#? [!O6]

loadsgf ./sgf/ko/ko.2.sgf

30 reg_genmove white
#? [B9]

40 reg_genmove white
#? [!P19|A11]
# both ko are bad here

loadsgf ./sgf/ko/ko.3.sgf

50 reg_genmove black
#? [!R5]

51 reg_genmove black
#? [S3]

55 reg_genmove white
#? [S3|R5]


loadsgf ./sgf/ko/ko.4.sgf

60 reg_genmove black
#? [O4]
# start ko to kill opponent and survive

70 reg_genmove black
#? [!Q8]
# not enough to live, even with another move.

loadsgf ./sgf/ko/ko.5.sgf

80 reg_genmove black
#? [!L6]
# bad ko connection, small

81 reg_genmove white
#? [!L6]
# bad ko capture, small

loadsgf ./sgf/ko/ko.6.sgf

90 reg_genmove black
#? [T10]
# fight ko for group

91 reg_genmove white
#? [T10]
# connect ko for group

100 reg_genmove black
#? [!T7]

loadsgf ./sgf/ko/ko.7.sgf

110 reg_genmove white
#? [T10]

111 reg_genmove white
#? [!T8]

loadsgf ./sgf/ko/ko.8.sgf

120 reg_genmove white
#? [T10]

121 reg_genmove white
#? [!Q7|Q10]

