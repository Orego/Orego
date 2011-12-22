#------------------------------------------------------------------------------
# Life and Death
#
# Computer Go Test Collection http://www.cs.ualberta.ca/~games/go/
#
# $Source: /Users/sofdev/cvsroot/Orego/scripts/cgtc/cgtc-2.0.1/life_death.tst,v $
# $Id: life_death.tst,v 1.1 2009/06/17 21:59:03 sofdev Exp $
#------------------------------------------------------------------------------

loadsgf ./sgf/life_death/life_death.3.sgf 1

60 reg_genmove white
#? [E18|K18]

loadsgf ./sgf/life_death/life_death.3.sgf

80 reg_genmove black
#? [K18]

loadsgf ./sgf/life_death/life_death.4.sgf

90 reg_genmove black
#? [B18]

loadsgf ./sgf/life_death/life_death.5.sgf

110 reg_genmove black
#? [J5]

loadsgf ./sgf/life_death/life_death.6.sgf

120 reg_genmove black
#? [A18]

loadsgf ./sgf/life_death/life_death.7.sgf

130 reg_genmove black
#? [N3]

131 reg_genmove white
#? [N3]

loadsgf ./sgf/life_death/life_death.8.sgf

140 reg_genmove black
#? [!O1]
# B is already alive. O1 is suicide.

loadsgf ./sgf/life_death/life_death.9.sgf

150 reg_genmove black
#? [K1|K2|N1|Q2]

151 reg_genmove white
#? [K2]

loadsgf ./sgf/life_death/life_death.10.sgf

160 reg_genmove white
#? [K2]

161 reg_genmove white
#? [!N1]

loadsgf ./sgf/life_death/life_death.11.sgf 1

170 reg_genmove black
#? [K2|Q2]

171 reg_genmove black
#? [!S1]

loadsgf ./sgf/life_death/life_death.12.sgf

190 reg_genmove white
#? [K1|K2]

200 reg_genmove white
#? [!Q2]

loadsgf ./sgf/life_death/life_death.13.sgf 1

210 reg_genmove black
#? [K2]

211 reg_genmove black
#? [!Q1]

loadsgf ./sgf/life_death/life_death.14.sgf

230 reg_genmove white
#? [K2|J1|J3|K1]

loadsgf ./sgf/life_death/life_death.15.sgf

240 reg_genmove white
#? [K2]

241 reg_genmove black
#? [K2]

loadsgf ./sgf/life_death/life_death.16.sgf

250 reg_genmove black
#? [!L1|M1|O4]
# dead group

loadsgf ./sgf/life_death/life_death.17.sgf

260 reg_genmove black
#? [O4]

270 reg_genmove black
#? [!Q8]

loadsgf ./sgf/life_death/life_death.18.sgf

280 reg_genmove black
#? [F19]

281 reg_genmove white
#? [F18|F19|G19|H19]

loadsgf ./sgf/life_death/life_death.19.sgf

290 reg_genmove black
#? [R11]
# can get a ko

300 reg_genmove black
#? [!S11]

loadsgf ./sgf/life_death/life_death.20.sgf

310 reg_genmove white
#? [R11|S11]

loadsgf ./sgf/life_death/life_death.21.sgf 1

320 reg_genmove white
#? [S15]

321 reg_genmove white
#? [!Q10]
# kill all, don't cut

loadsgf ./sgf/life_death/life_death.21.sgf

330 reg_genmove black
#? [S15]
# T12 also lives but S15 is bigger

loadsgf ./sgf/life_death/life_death.22.sgf

340 reg_genmove white
#? [S15]
# T12 also kills but S15 is better

loadsgf ./sgf/life_death/life_death.23.sgf

350 reg_genmove black
#? [T10]

351 reg_genmove black
#? [!T9|T12|T13]

355 reg_genmove white
#? [T9]

loadsgf ./sgf/life_death/life_death.24.sgf

360 reg_genmove white
#? [S12]

361 reg_genmove black
#? [R12]

370 reg_genmove white
#? [!T7|R12]

