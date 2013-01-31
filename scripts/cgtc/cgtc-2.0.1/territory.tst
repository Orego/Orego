#------------------------------------------------------------------------------
# Make, Defend, Reduce and Invade Territory
#
# Computer Go Test Collection http://www.cs.ualberta.ca/~games/go/
#
# $Source: /Users/sofdev/cvsroot/Orego/scripts/cgtc/cgtc-2.0.1/territory.tst,v $
# $Id: territory.tst,v 1.1 2009/06/17 21:59:04 sofdev Exp $
#------------------------------------------------------------------------------

loadsgf ./sgf/territory/territory.1.sgf 1

10 reg_genmove white
#? [F17|G17|J16|J17|K16|K17]


loadsgf ./sgf/territory/territory.1.sgf

30 reg_genmove black
#? [F17|J16|J17|K16|K17]

loadsgf ./sgf/territory/territory.2.sgf

40 reg_genmove black
#? [F17|J17|J16|K16|K17]

loadsgf ./sgf/territory/territory.3.sgf

60 reg_genmove black
#? [K17|K16]

loadsgf ./sgf/territory/territory.4.sgf

80 reg_genmove white
#? [L14|K6|L13|K13|K9|K10|L10|L6|K12|K11|L11|L12|L9|K8|L8|L7|K7]


loadsgf ./sgf/territory/territory.5.sgf 1

100 reg_genmove white
#? [C4]

loadsgf ./sgf/territory/territory.5.sgf

120 reg_genmove black
#? [C4]

loadsgf ./sgf/territory/territory.6.sgf

130 reg_genmove white
#? [S5]

loadsgf ./sgf/territory/territory.7.sgf

150 reg_genmove black
#? [T4|B16]

loadsgf ./sgf/territory/territory.8.sgf

170 reg_genmove black
#? [C4]

171 reg_genmove white
#? [D2|C4]

loadsgf ./sgf/territory/territory.9.sgf

180 reg_genmove white
#? [O4|Q6|P10|M15|J6|E11|E10]

loadsgf ./sgf/territory/territory.10.sgf 1

201 reg_genmove white
#? [R5]

loadsgf ./sgf/territory/territory.10.sgf

210 reg_genmove black
#? [R4]
# Settle group, leave the white stone on the left side

loadsgf ./sgf/territory/territory.11.sgf

220 reg_genmove black
#? [C4]

221 reg_genmove white
#? [C4]

loadsgf ./sgf/territory/territory.12.sgf

230 reg_genmove white
#? [S16]

231 reg_genmove black
#? [S15|S16]

loadsgf ./sgf/territory/territory.13.sgf

250 reg_genmove white
#? [F9|G10|H10|K10]
# escaping/reducing the center is big

loadsgf ./sgf/territory/territory.14.sgf

270 reg_genmove white
#? [D2]
# It may also be possible to start the B13 ko but it is complicated

280 reg_genmove white
#? [!B5|K9|O19|A11]

loadsgf ./sgf/territory/territory.15.sgf

290 reg_genmove white
#? [B9]

300 reg_genmove white
#? [!A11|P19]

loadsgf ./sgf/territory/territory.16.sgf

310 reg_genmove white
#? [B9]

320 reg_genmove white
#? [!B10|A8|A11|B8]

loadsgf ./sgf/territory/territory.17.sgf

330 reg_genmove white
#? [Q17|R17]
# attacking this group is biggest - this should become territory

331 reg_genmove white
#? [!C8]
# also big, but these will not die

loadsgf ./sgf/territory/territory.18.sgf

340 reg_genmove white
#? [R17]
# attacking this group is biggest

loadsgf ./sgf/territory/territory.19.sgf

350 reg_genmove white
#? [B18|G17|H17|H18|J18]


loadsgf ./sgf/territory/territory.20.sgf

370 reg_genmove white
#? [H18|J4]

loadsgf ./sgf/territory/territory.21.sgf

390 reg_genmove white
#? [J2]
# defend space, group is getting small.

loadsgf ./sgf/territory/territory.22.sgf

410 reg_genmove white
#? [B9]

411 reg_genmove white
#? [!A9]
# B can counter with B9

loadsgf ./sgf/territory/territory.23.sgf

420 reg_genmove white
#? [J2|G2]

loadsgf ./sgf/territory/territory.24.sgf

440 reg_genmove white
#? [C10|D9]

loadsgf ./sgf/territory/territory.25.sgf

460 reg_genmove white
#? [C4]

loadsgf ./sgf/territory/territory.26.sgf

470 reg_genmove white
#? [B2|R3|S7]

loadsgf ./sgf/territory/territory.27.sgf

480 reg_genmove black
#? [R17|R7|H17|J3]

loadsgf ./sgf/territory/territory.28.sgf

500 reg_genmove black
#? [R16|Q17]

loadsgf ./sgf/territory/territory.29.sgf 1

520 reg_genmove white
#? [K15|M17|O13]

loadsgf ./sgf/territory/territory.29.sgf

530 reg_genmove black
#? [Q12|K15|Q13|P14]

loadsgf ./sgf/territory/territory.30.sgf

540 reg_genmove black
#? [D14|C14|R13|C7|C8|R6|O3|L3]

loadsgf ./sgf/territory/territory.31.sgf

560 reg_genmove black
#? [D17|R3|Q3]

loadsgf ./sgf/territory/territory.32.sgf

580 reg_genmove black
#? [R11|S11|S13]

loadsgf ./sgf/territory/territory.33.sgf

600 reg_genmove black
#? [R3|P4]

loadsgf ./sgf/territory/territory.34.sgf

610 reg_genmove white
#? [D18|E18|S5|P3|Q3]

loadsgf ./sgf/territory/territory.35.sgf

620 reg_genmove black
#? [M8|R3|S3]

loadsgf ./sgf/territory/territory.36.sgf

640 reg_genmove black
#? [H17]

641 reg_genmove black
#? [!J16]

loadsgf ./sgf/territory/territory.37.sgf

650 reg_genmove black
#? [P7]
#defend eye space

loadsgf ./sgf/territory/territory.38.sgf

660 reg_genmove black
#? [K3|K4]

loadsgf ./sgf/territory/territory.39.sgf

670 reg_genmove black
#? [M17]

loadsgf ./sgf/territory/territory.40.sgf

680 reg_genmove black
#? [J4|K4|L4|J3|K3|M17|R7|Q7]

loadsgf ./sgf/territory/territory.41.sgf

690 reg_genmove black
#? [!E7]

loadsgf ./sgf/territory/territory.42.sgf

700 reg_genmove black
#? [!H18]

loadsgf ./sgf/territory/territory.43.sgf

710 reg_genmove black
#? [E2]

loadsgf ./sgf/territory/territory.44.sgf

720 reg_genmove black
#? [G4]

loadsgf ./sgf/territory/territory.45.sgf

730 reg_genmove black
#? [M4]
# urgent gap, nonblockable if W pushes.

731 reg_genmove white
#? [M4]

loadsgf ./sgf/territory/territory.46.sgf

740 reg_genmove white
#? [M4]
# urgent defense

loadsgf ./sgf/territory/territory.47.sgf 1

760 reg_genmove white
#? [M18|D2|E2|O15|O16]

770 reg_genmove white
#? [!F13]

loadsgf ./sgf/territory/territory.48.sgf

800 reg_genmove white
#? [C15]

loadsgf ./sgf/territory/territory.50.sgf

810 reg_genmove black
#? [K14|L14|L13|L10|L11|M10|M11|P10|O10]

loadsgf ./sgf/territory/territory.51.sgf

830 reg_genmove black
#? [D17|L7|N6|P10|O10]

loadsgf ./sgf/territory/territory.54.sgf

860 reg_genmove black
#? [L13]

loadsgf ./sgf/territory/territory.55.sgf

870 reg_genmove white
#? [P18]

871 reg_genmove black
#? [P18]

loadsgf ./sgf/territory/territory.57.sgf 1

880 reg_genmove white
#? [D3|C2]

loadsgf ./sgf/territory/territory.57.sgf

900 reg_genmove black
#? [D3]

loadsgf ./sgf/territory/territory.60.sgf

910 reg_genmove white
#? [C3]

loadsgf ./sgf/territory/territory.61.sgf

920 reg_genmove black
#? [B2|K18]

loadsgf ./sgf/territory/territory.62.sgf

940 reg_genmove black
#? [R10|Q10|C10|C11|D11|D10|Q11|R11]

loadsgf ./sgf/territory/territory.63.sgf 1

960 reg_genmove black
#? [J15|H16|C12|Q14]

loadsgf ./sgf/territory/territory.63.sgf

980 reg_genmove white
#? [D9|D8|E9]


loadsgf ./sgf/territory/territory.64.sgf

1010 reg_genmove black
#? [O10|P10|L7|N6]

loadsgf ./sgf/territory/territory.65.sgf

1020 reg_genmove black
#? [E17|F17|P3|P4|E16|F16|O3|O4|Q15|R13]

loadsgf ./sgf/territory/territory.66.sgf

1030 reg_genmove black
#? [C14|E17|C18|D17]

loadsgf ./sgf/territory/territory.67.sgf

1060 reg_genmove white
#? [E17|C13|O5]

loadsgf ./sgf/territory/territory.68.sgf

1070 reg_genmove black
#? [L5|L4]

loadsgf ./sgf/territory/territory.69.sgf 1

1080 reg_genmove white
#? [E14|M12|N12|M16|L5|L6]


loadsgf ./sgf/territory/territory.69.sgf

1100 reg_genmove black
#? [M16|L14|N15]

loadsgf ./sgf/territory/territory.70.sgf

1110 reg_genmove black
#? [L5]

1120 reg_genmove black
#? [!M4]

loadsgf ./sgf/territory/territory.71.sgf

1130 reg_genmove white
#? [L5]

1131 reg_genmove white
#? [!Q5]

loadsgf ./sgf/territory/territory.72.sgf

1140 reg_genmove black
#? [L5]

1141 reg_genmove white
#? [L5]

loadsgf ./sgf/territory/territory.73.sgf

1150 reg_genmove white
#? [G16|H17]

loadsgf ./sgf/territory/territory.74.sgf

1170 reg_genmove white
#? [H9|J10|J11]

loadsgf ./sgf/territory/territory.75.sgf

1190 reg_genmove white
#? [N14]

1191 reg_genmove black
#? [N14]

1200 reg_genmove white
#? [!G3|H2]

loadsgf ./sgf/territory/territory.76.sgf

1210 reg_genmove black
#? [N14]

1211 reg_genmove white
#? [N14]

loadsgf ./sgf/territory/territory.77.sgf

1220 reg_genmove white
#? [C12|G3|H3|Q3|Q6|Q11]

loadsgf ./sgf/territory/territory.78.sgf

1230 reg_genmove white
#? [J4|K5|P3|Q11|Q12|E12]

1240 reg_genmove white
#? [!C8|Q18]

loadsgf ./sgf/territory/territory.79.sgf

1250 reg_genmove black
#? [O10|O11|S7|S8|Q7]

loadsgf ./sgf/territory/territory.80.sgf

1260 reg_genmove black
#? [K12]

1261 reg_genmove white
#? [K12|L13]

loadsgf ./sgf/territory/territory.81.sgf

1280 reg_genmove white
#? [K14|K15|O5]

