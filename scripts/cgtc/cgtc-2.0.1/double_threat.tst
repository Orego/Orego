#------------------------------------------------------------------------------
# Double Threats
#
# Computer Go Test Collection http://www.cs.ualberta.ca/~games/go/
#
# $Source: /Users/sofdev/cvsroot/Orego/scripts/cgtc/cgtc-2.0.1/double_threat.tst,v $
# $Id: double_threat.tst,v 1.1 2009/06/17 21:59:04 sofdev Exp $
#------------------------------------------------------------------------------

loadsgf ./sgf/double_threat/double_threat.1.sgf

10 reg_genmove black
#? [H8]

11 reg_genmove white
#? [J7|H8]

loadsgf ./sgf/double_threat/double_threat.2.sgf

20 reg_genmove white
#? [K9]

21 reg_genmove black
#? [K8|K9]

