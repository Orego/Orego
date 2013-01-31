#------------------------------------------------------------------------------
# Endgame
#
# Computer Go Test Collection http://www.cs.ualberta.ca/~games/go/
#
# $Source: /Users/sofdev/cvsroot/Orego/scripts/cgtc/cgtc-2.0.1/endgame.tst,v $
# $Id: endgame.tst,v 1.1 2009/06/17 21:59:04 sofdev Exp $
#------------------------------------------------------------------------------

loadsgf ./sgf/endgame/endgame.1.sgf

10 reg_genmove black
#? [J18]
# seems better than playing a monkey jump immediately

loadsgf ./sgf/endgame/endgame.3.sgf

30 reg_genmove black
#? [T4|B16]

40 reg_genmove black
#? [!T5]

loadsgf ./sgf/endgame/endgame.4.sgf

50 reg_genmove black
#? [E2]

51 reg_genmove white
#? [D2|K2]

loadsgf ./sgf/endgame/endgame.5.sgf

60 reg_genmove black
#? [G4|A11]

loadsgf ./sgf/endgame/endgame.6.sgf

70 reg_genmove black
#? [D3]

71 reg_genmove white
#? [D3]

loadsgf ./sgf/endgame/endgame.8.sgf

80 reg_genmove black
#? [C3]

81 reg_genmove white
#? [C3]

loadsgf ./sgf/endgame/endgame.9.sgf

90 reg_genmove black
#? [B2]

loadsgf ./sgf/endgame/endgame.10.sgf

110 reg_genmove black
#? [H6]

111 reg_genmove white
#? [H6]

