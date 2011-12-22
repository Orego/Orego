#------------------------------------------------------------------------------
# Cut and Connect
#
# Computer Go Test Collection http://www.cs.ualberta.ca/~games/go/
# 
# $Source: /Users/sofdev/cvsroot/Orego/scripts/cgtc/cgtc-2.0.1/cut_connect.tst,v $
# $Id: cut_connect.tst,v 1.1 2009/06/17 21:59:04 sofdev Exp $
#------------------------------------------------------------------------------

loadsgf ./sgf/cut_connect/cut_connect.1.sgf

10 reg_genmove black
#? [Q12]

loadsgf ./sgf/cut_connect/cut_connect.2.sgf

30 reg_genmove black
#? [Q12|Q15]

40 reg_genmove black
#? [!S12]

loadsgf ./sgf/cut_connect/cut_connect.3.sgf

50 reg_genmove white
#? [B15]

60 reg_genmove black
#? [B15]

loadsgf ./sgf/cut_connect/cut_connect.4.sgf 1

70 reg_genmove white
#? [C4]

71 reg_genmove black
#? [C4]

80 reg_genmove white
#? [!C2]

loadsgf ./sgf/cut_connect/cut_connect.4.sgf

90 reg_genmove black
#? [C4]

loadsgf ./sgf/cut_connect/cut_connect.5.sgf

100 reg_genmove black
#? [C4]

loadsgf ./sgf/cut_connect/cut_connect.6.sgf

120 reg_genmove white
#? [K15]

121 reg_genmove black
#? [K15]

130 reg_genmove white
#? [!D11]

loadsgf ./sgf/cut_connect/cut_connect.7.sgf 1

140 reg_genmove white
#? [K16|J15]

150 reg_genmove white
#? [!G13]

loadsgf ./sgf/cut_connect/cut_connect.7.sgf

160 reg_genmove black
#? [K16|J15]

loadsgf ./sgf/cut_connect/cut_connect.9.sgf

190 reg_genmove black
#? [M16]

200 reg_genmove black
#? [!M15]

loadsgf ./sgf/cut_connect/cut_connect.10.sgf

210 reg_genmove white
#? [N7]

211 reg_genmove black
#? [N7]

220 reg_genmove white
#? [!L6]

loadsgf ./sgf/cut_connect/cut_connect.11.sgf 1

230 reg_genmove black
#? [Q15]

loadsgf ./sgf/cut_connect/cut_connect.11.sgf 1

240 reg_genmove white
#? [Q16|R14]

loadsgf ./sgf/cut_connect/cut_connect.13.sgf

260 reg_genmove black
#? [D4]

261 reg_genmove white
#? [D4]

loadsgf ./sgf/cut_connect/cut_connect.14.sgf

270 reg_genmove black
#? [R10]

loadsgf ./sgf/cut_connect/cut_connect.15.sgf

280 reg_genmove black
#? [N13|P13|O11]

290 reg_genmove black
#? [!H18]

loadsgf ./sgf/cut_connect/cut_connect.16.sgf

300 reg_genmove black
#? [P17]

301 reg_genmove black
#? [!P18]

loadsgf ./sgf/cut_connect/cut_connect.17.sgf

310 reg_genmove black
#? [Q18|Q15]

loadsgf ./sgf/cut_connect/cut_connect.18.sgf

320 reg_genmove black
#? [P10]

loadsgf ./sgf/cut_connect/cut_connect.19.sgf

330 reg_genmove white
#? [!R15]

loadsgf ./sgf/cut_connect/cut_connect.20.sgf

340 reg_genmove white
#? [F14]

350 reg_genmove white
#? [!F12|G15]

loadsgf ./sgf/cut_connect/cut_connect.21.sgf

360 reg_genmove white
#? [J5]

361 reg_genmove black
#? [J5]

370 reg_genmove white
#? [!P5]

loadsgf ./sgf/cut_connect/cut_connect.23.sgf

390 reg_genmove white
#? [S3|Q2|S2]

391 reg_genmove black
#? [S2]

400 reg_genmove white
#? [!A11]

loadsgf ./sgf/cut_connect/cut_connect.26.sgf

430 reg_genmove white
#? [R15]

431 reg_genmove black
#? [R15]

loadsgf ./sgf/cut_connect/cut_connect.27.sgf

440 reg_genmove black
#? [R15]

loadsgf ./sgf/cut_connect/cut_connect.28.sgf

450 reg_genmove white
#? [J9]

loadsgf ./sgf/cut_connect/cut_connect.29.sgf

460 reg_genmove white
#? [K10]

loadsgf ./sgf/cut_connect/cut_connect.31.sgf

470 reg_genmove black
#? [O18]

471 reg_genmove white
#? [O18]

loadsgf ./sgf/cut_connect/cut_connect.33.sgf

480 reg_genmove black
#? [R15|Q14|Q15]

481 reg_genmove white
#? [R15|Q14]
# this is a rather messy position but
# W can get something if B does not defend.

482 reg_genmove black
#? [!C2]

483 reg_genmove white
#? [!C2]

loadsgf ./sgf/cut_connect/cut_connect.34.sgf

490 reg_genmove black
#? [O7]

loadsgf ./sgf/cut_connect/cut_connect.35.sgf

500 reg_genmove white
#? [K2]

501 reg_genmove black
#? [K2]

loadsgf ./sgf/cut_connect/cut_connect.36.sgf

510 reg_genmove black
#? [K18]

loadsgf ./sgf/cut_connect/cut_connect.37.sgf

520 reg_genmove white
#? [Q14|N14]

loadsgf ./sgf/cut_connect/cut_connect.38.sgf

530 reg_genmove black
#? [T8]

loadsgf ./sgf/cut_connect/cut_connect.39.sgf

540 reg_genmove black
#? [L12|M11|N10]
# L12 is an indirect defense of the cut

541 reg_genmove black
#? [!K10]

loadsgf ./sgf/cut_connect/cut_connect.40.sgf

550 reg_genmove white
#? [M11]

loadsgf ./sgf/cut_connect/cut_connect.41.sgf 1

560 reg_genmove white
#? [S15]
# don't cut, kill all.

561 reg_genmove white
#? [!Q10]
# don't cut, kill all.

loadsgf ./sgf/cut_connect/cut_connect.42.sgf

580 reg_genmove black
#? [J6]

581 reg_genmove white
#? [J6]

loadsgf ./sgf/cut_connect/cut_connect.43.sgf

590 reg_genmove white
#? [N6]

600 reg_genmove white
#? [!O7]

loadsgf ./sgf/cut_connect/cut_connect.44.sgf

610 reg_genmove black
#? [N6]

loadsgf ./sgf/cut_connect/cut_connect.45.sgf

620 reg_genmove black
#? [C15]

630 reg_genmove black
#? [!E14|D13|E13]
# bad connection, capture instead

loadsgf ./sgf/cut_connect/cut_connect.47.sgf

660 reg_genmove black
#? [K5]

loadsgf ./sgf/cut_connect/cut_connect.48.sgf

670 reg_genmove black
#? [J5]

loadsgf ./sgf/cut_connect/cut_connect.49.sgf 1

680 reg_genmove black
#? [!L11]
# bad cut

loadsgf ./sgf/cut_connect/cut_connect.49.sgf

681 reg_genmove white
#? [L12]
# capture cutting stone

loadsgf ./sgf/cut_connect/cut_connect.50.sgf

690 reg_genmove white
#? [B8]

loadsgf ./sgf/cut_connect/cut_connect.51.sgf

700 reg_genmove white
#? [R7]

loadsgf ./sgf/cut_connect/cut_connect.52.sgf 1

710 reg_genmove white
#? [R7]

loadsgf ./sgf/cut_connect/cut_connect.52.sgf

720 reg_genmove black
#? [R6|R7|S7]

loadsgf ./sgf/cut_connect/cut_connect.53.sgf 1

730 reg_genmove black
#? [R6|R7|S7]

740 reg_genmove black
#? [!O9]

loadsgf ./sgf/cut_connect/cut_connect.53.sgf

750 reg_genmove white
#? [P11|Q11]

loadsgf ./sgf/cut_connect/cut_connect.54.sgf

760 reg_genmove black
#? [P10]

770 reg_genmove black
#? [!F2]

loadsgf ./sgf/cut_connect/cut_connect.55.sgf

780 reg_genmove black
#? [R7]

loadsgf ./sgf/cut_connect/cut_connect.56.sgf

790 reg_genmove white
#? [Q6]

loadsgf ./sgf/cut_connect/cut_connect.57.sgf

800 reg_genmove black
#? [L4]

loadsgf ./sgf/cut_connect/cut_connect.58.sgf 1

810 reg_genmove white
#? [L6]

811 reg_genmove white
#? [!K6]

loadsgf ./sgf/cut_connect/cut_connect.58.sgf

820 reg_genmove black
#? [L6]

loadsgf ./sgf/cut_connect/cut_connect.60.sgf

840 reg_genmove black
#? [H4]

850 reg_genmove white
#? [H4]

loadsgf ./sgf/cut_connect/cut_connect.61.sgf 1

860 reg_genmove white
#? [H4]

loadsgf ./sgf/cut_connect/cut_connect.61.sgf

870 reg_genmove black
#? [H4|J5]

loadsgf ./sgf/cut_connect/cut_connect.63.sgf

900 reg_genmove white
#? [H4]

loadsgf ./sgf/cut_connect/cut_connect.64.sgf

910 reg_genmove black
#? [G5]

911 reg_genmove white
#? [G5|H5|G6]

loadsgf ./sgf/cut_connect/cut_connect.65.sgf

920 reg_genmove white
#? [E12]

930 reg_genmove white
#? [!G12]

loadsgf ./sgf/cut_connect/cut_connect.66.sgf 1

940 reg_genmove black
#? [E12]

loadsgf ./sgf/cut_connect/cut_connect.66.sgf

950 reg_genmove white
#? [G13]

loadsgf ./sgf/cut_connect/cut_connect.67.sgf

970 reg_genmove white
#? [E8]

loadsgf ./sgf/cut_connect/cut_connect.68.sgf

980 reg_genmove white
#? [F9]

loadsgf ./sgf/cut_connect/cut_connect.69.sgf

990 reg_genmove white
#? [M14]

1010 reg_genmove black
#? [M14]

loadsgf ./sgf/cut_connect/cut_connect.71.sgf 1

1020 reg_genmove black
#? [Q6]

loadsgf ./sgf/cut_connect/cut_connect.71.sgf

1025 reg_genmove white
#? [R7|Q6|Q3]

loadsgf ./sgf/cut_connect/cut_connect.72.sgf

1030 reg_genmove white
#? [Q6]

1031 reg_genmove black
#? [Q6]

loadsgf ./sgf/cut_connect/cut_connect.73.sgf

1040 reg_genmove white
#? [B11]

loadsgf ./sgf/cut_connect/cut_connect.74.sgf

1050 reg_genmove white
#? [S7]

loadsgf ./sgf/cut_connect/cut_connect.75.sgf 1

1060 reg_genmove black
#? [D13|D14|E13]

loadsgf ./sgf/cut_connect/cut_connect.75.sgf

1070 reg_genmove white
#? [D13]

loadsgf ./sgf/cut_connect/cut_connect.77.sgf

1100 reg_genmove white
#? [D13]

1110 reg_genmove white
#? [!B14]

