# Parcours vers 3.0 — consolidation approuvée, aucune publication autorisée

## Nouvelle tranche autorisée — catalogues GOV 001–004

Mandat courant : intégrer les variantes des quatre catalogues au générateur,
référence GOV `60d1c9453aaf91f1e90a6ba113f106ac51238cf9`, sur la branche existante
PR64. La précédente conclusion « seul le client reste » ne clôt pas ce nouveau mandat.
Première tranche : composeur voxel BP-05 A/C puis raccord au chemin naturel
existant pour les quatre accès. Le puits rejoint E au-dessus du plafond, sans
écraser N3 ; profondeur 20–28, rangs/budgets et limites de chunk conservés.
Voir [SITE_BLUEPRINT_INTEGRATION.md](SITE_BLUEPRINT_INTEGRATION.md) pour les hashes,
le raccord, les tests et le plafond composé inchangé de 2 500 cellules.
Le local de relevé est conservé ; le puits sous son entrée est compacté à deux
blocs de largeur, avec son échelle soutenue, pour respecter ce plafond.
Le raccord A/C `724e8d6` a 16 jobs verts, 716 JUnit et 83+83 GameTests par pile,
plus streaming/reprise ; maximum composé 2 474 sur 1 440 plans, local conservé.
Son JAR non installé a le SHA256 `8ae0701673502ce7b7bc1d407225a9f2fa0b9e984e9f2e80b39ed3266eb4aa2e`.
BP-05 A/B/C `768e8c4` est qualifié : 16 jobs verts, 716 JUnit, 83+83 GameTests
par pile et streaming/reprise ; maximum 2 482 sur 2 160 plans. JAR non installé :
`26149d8a06056f87b44b6283fc22744e2df99c6a263f8158326abbaff3a892a5`.
BP-16 A `2f2d16a` est qualifié : 16 jobs verts, 716 JUnit, 84+84 GameTests par
pile et streaming/reprise ; maximum 2 487 sur 2 880 plans. JAR non installé :
`8bcee2f6efd43fcfe8837e1066ef33ec8dfd787abe1c73f84d4653d055e5b1fa`.
BP-16 B/C `f5478f0` est qualifié : 16 jobs verts, 716 JUnit, 86+86 GameTests
par pile et streaming/reprise ; maximum 2 482 sur 4 320 plans. JAR non installé :
`b21ab05e3c15f61e90ac322e1c161434f8ac5ecce4911378d02112af654851c8`.
BP-27 A/B/C `4f0eeeb` est qualifié : 16 jobs verts, 716 JUnit, 88+88 GameTests
par pile, streaming/reprise ; maximum 2 482 sur 6 480 plans. JAR non installé :
`a709d07f03b52c4eb0395d5fb7d88ee06aeb94b0c316b86abe03ff7d7d74a3dc`.
BP-37 A/B/C `8332c19` est qualifié : 16 jobs verts, 716 JUnit, 90+90 GameTests
par pile et streaming/reprise ; maximum 2 482 sur 8 640 plans. JAR non installé :
`d7d94ed7d3e411a29adc7f4b613f7a0114176dda0673cf8a2cb217d380d7df94`.
BP-03 A/B/C `82a9e16` est qualifié : 16 jobs verts, 716 JUnit, 92+92 GameTests
par pile et streaming/reprise ; maximum 2 488 sur 10 800 plans. JAR non installé :
`5320a99af024ad1bc4a3681512b982a120ba05d66ca53df91d90055a312de91f`.
BP-23 A/B/C `cf96d07` est qualifié : 16 jobs verts, 716 JUnit, 94+94 GameTests
par pile et streaming/reprise ; maximum 2 488 sur 12 960 plans. JAR non installé :
`2ac0f1436786cb96c33e0bf24fa0f304daa7757a2d665d3280c0dc9ee509df1a`.
BP-02 A/B/C `eef708f` est qualifié : 16 jobs verts, 716 JUnit,
96+96 GameTests par pile et streaming/reprise ; maximum 2500 sur 15 120 plans.
JAR non installé : `10d830bd1ca97ef4506e2ea301e2b7800b2539597bba4e516fa2ccfa8252130d`.
BP-06 A/B/C `6c47363` est qualifié : 16 jobs verts, 716 JUnit,
98+98 GameTests par pile et streaming/reprise ; maximum 2499 sur 17 280 plans.
JAR non installé : `7f9d00ed6c2eda7c1238414c1c756d7b59be9c46f14bf88b0bfd33ef82aee16f`.
BP-10 A/B/C a9b3ad2 est qualifié : 16 jobs verts, 716 JUnit,
100+100 GameTests par pile et streaming/reprise ; maximum 2499 sur 19 440 plans.
JAR non installé : 8aa9a9671d3c2c50f1ab5fb283eeda31a2f584c566f3dfcd607c74c6ae43f933.
Cycle suivant : BP-14 A/B/C, trois rainures parallèles à hauteurs différentes,
collecteur décalé avec escalier, fenêtre basse B et massif avec détour C.
Positions, masques, budgets, récompenses et fonctions conservés. Matrice de
21 600 plans, 120 placements réels ; coupures des connexions et du détour,
compensation avec édition étrangère. Qualification exacte à suivre dans PR64,
sans transfert des preuves précédentes. Couverture 30/120, 90 restantes.
Plafond 2 500 inchangé. Aucun accès PC, activation Nether, fusion ou publication.
Visuel/client non prouvé.

### BP-14 — régression de fixture MINER_CAMP

Le candidat 357792c respecte le plafond (maximum mesuré 2 494), mais son test de
placement réel échoue sur COMB. Sa coque ouest atteint X=0 ; la marge anti-collision
s’étend au chunk voisin dont la fixture ne préparait pas les métadonnées.
Le test prépare désormais explicitement ce halo ; un test négatif distinct exige
le refus du voisin absent, vérifie l’absence de chargement forcé, puis l’admission
une fois les seules métadonnées de fixture disponibles. Aucun garde-fou de production
ni géométrie modifié dans cette correction. Qualification du nouveau source à suivre
sur PR64 ; les échecs précédents ne sont pas des preuves de réussite.

### BP-14 qualifié ; cycle suivant BP-24 A/B/C

BP-14 accepté sur 17fc48887358e8ffb94a6cb4b67cb85b115b0cda : 16 jobs verts,
716 JUnit et 103+103 GameTests par pile, streaming/reprise ; 21 600 plans,
120 placements réels, maximum 2 494. JAR non installé, SHA256
8f42a09a7e420b1818b00940cb769af51d9db847e3d5217f0d44e36fa2b3fe78.
Le refus MINER_CAMP/COMB était le halo de métadonnées absent dans la fixture ;
le test négatif confirme le refus sans chargement forcé, puis l’admission.

Suite autorisée : BP-24 A/B/C, becs alternés attachés aux parois, fenêtre médiane
B et encoche haute C. Positions, masques, fonctions, accès et budgets conservés ;
23 760 plans, 132 placements réels et coupures de parcours dans les deux sens.
Couverture 33/120, 87 restantes. Qualification exacte à suivre dans PR64 ; les
preuves BP-14 ne sont pas transférées. Plafond 2 500 inchangé, aucun accès PC,
installation, fusion, publication ou activation Nether. Visuel/client non prouvé.

### BP-24 qualifié ; cycle suivant BP-35 A/B/C

BP-24 accepté sur 33d7d3fd5f20fb04599c12f00c9b0543892f2860 : 16 jobs verts,
716 JUnit et 105+105 GameTests par pile, streaming/reprise ; 23 760 plans,
132 placements réels, maximum 2500.
JAR non installé, SHA256 30d8c8176bd7d98b8f14224accdab9fbbacf7dbe5fe5437c40a4c8c9b8a2b7cb.

Suite autorisée : BP-35 A/B/C, cheminée visible et desserte protégée, balcon
médian B et descente arrière C. Masques, positions, accès, fonctions et budgets
conservés ; 25 920 plans et 144 placements réels, coupures dans les deux sens.
Couverture 36/120, 84 restantes. Qualification exacte à suivre dans PR64 sans
transférer les preuves précédentes. Plafond 2 500 inchangé ; aucun accès PC,
installation, fusion, publication ou activation Nether. Visuel/client non prouvé.

### BP-35 qualifié ; cycle suivant BP-31 A/B/C

BP-35 accepté sur 8cda8fb3b227e7cffc3d6497b4692ccb1aeb4ae1 : 16 jobs verts,
716 JUnit et 107+107 GameTests par pile, streaming/reprise ; 25 920 plans,
144 placements réels, maximum 2488.
JAR non installé, SHA256 60785441781d6b2e916c11e0e51881011c121dceefe7b6bfdaabe1240020cbc9.

Suite autorisée : BP-31 A/B/C, vestibule creux à six directions, retour diagonal
B et bras haut coudé C avec palier et fenêtre protégée. Masques, positions,
fonctions, accès et budgets conservés ; 28 080 plans et 156 placements réels.
Couverture 39/120, 81 restantes. Qualification exacte à suivre dans PR64 sans
transfert des preuves précédentes. Plafond 2 500 inchangé ; aucun accès PC,
installation, fusion, publication ou activation Nether. Visuel/client non prouvé.


BP-31 review correction before qualification: candidate 2686f4f had a one-cell
corner beside the lower shaft's backing. The west arm now enters north of that
backing into a wider hollow vestibule; its two-wide support is asserted explicitly,
as are the north/east arms. The north endpoint is offset to the east, while the
fixed north mineral alcove remains a recess of the vestibule. B's outer return
follows that endpoint. No mineral position, mask, budget or write cap is changed.
The new exact-source tests replace the unqualified candidate's runs.


The initial BP-31 candidate 2686f4f also failed the positive GeOre-family fixture
for DRY Aluminum. With 39 forms, DRY seed 2 selects BP-24 A (ordinal 30), whose
west root requires neighboring collision metadata. The catalogue fixture already
prepared this halo; the older family fixtures did not. A shared test-only helper
now prepares the exact selected geometry's metadata halo for positive camp tests.
The absent-neighbor negative test and production fail-closed/no-load checks remain
unchanged. Initial candidate matrix maximum was 2,497; no success is transferred.

### BP-31 qualifié ; cycle suivant BP-26 A/B/C

BP-31 accepté sur 05d8587fdd77e367b9f43bb8e7d1489e8f8f3670 : 16 jobs verts,
716 JUnit et 109+109 GameTests par pile, streaming/reprise ; 28 080 plans,
156 placements réels, maximum 2497.
JAR non installé, SHA256 57461f20eb775238abdf7e47b736eec465c06916df2f8afd76fc7c3649352983.

Suite autorisée : BP-26 A/B/C, pied rocheux enraciné et chapeau visitable,
fente radiale B avec descente et deux lobes C avec isthme/passage inférieur.
Masques, positions, accès, fonctions et budgets conservés ; 30 240 plans et
168 placements réels. Couverture 42/120, 78 restantes. Qualification exacte à
suivre dans PR64 sans transfert de preuves. Plafond 2 500 inchangé ; aucun accès
PC, installation, fusion, publication ou activation Nether. Visuel/client non prouvé.

### BP-26 qualifié ; cycle suivant BP-36 A/B/C

BP-26 accepté sur 116aac3dbd6f98e509cd04ee5d729afe74bd73c2 : 16 jobs verts,
716 JUnit et 111+111 GameTests par pile, streaming/reprise ; 30 240 plans,
168 placements réels, maximum 2494.
JAR non installé, SHA256 e57ceff931766dcdebfb10bd42de59c011796f25aa89e4c1f1ba065c0fac23d5.

Suite : BP-36 A/B/C, dalle percée de trois regards et deux volumes partagés.
Deux descentes A, troisième descente B, fente commune et nervure supérieure C.
Masques, accès, fonctions et budgets inchangés ; 32 400 plans et 180 placements.
Couverture 45/120, 75 restantes. Qualification exacte à suivre, aucun transfert
de preuves. Plafond 2 500 inchangé ; pas de PC, installation, fusion, publication
ou activation Nether. Validation visuelle/client non prouvée.

### BP-36 qualifié ; cycle suivant BP-11 A/B/C

BP-36 accepté sur 0c7386ddbbf8df75f00ae8fbb32f020966f25f42 : 16 jobs verts,
716 JUnit et 113+113 GameTests par pile, streaming/reprise ; 32 400 plans,
180 placements réels, maximum 2499.
JAR non installé, SHA256 073616dc2c66f37e9c12667d89e7f5e2b20c8f10d3101b690f149d87d68860a4.

Suite : BP-11 A/B/C, deux entonnoirs excentrés avec taille étroite.
Une descente A, deux B autour d'une lame, balcon C et descente latérale.
Masques, accès, fonctions et budgets inchangés ; 34 560 plans et 192 placements.
Couverture 48/120, 72 restantes. Qualification exacte à suivre sans transfert
de preuves. Plafond 2 500 inchangé ; pas de PC, installation, fusion, publication
ou activation Nether. Validation visuelle/client non prouvée.

### BP-11 qualifié ; cycle suivant BP-17 A/B/C

BP-11 accepté sur 03aa1afb2b6bd6f726d90e692d16e8266b57cc9b : 16 jobs verts, 716 JUnit et 115+115
GameTests par pile, streaming/reprise ; 34 560 plans, 192 placements réels,
maximum 2499. JAR non installé :
d2754df8654da3d53a4d8d204b1389240ec5ed85c00683daf0e2c8035ed1d209.

BP-17 : noyau attaché au plafond et pointes libres, passage inférieur de trois
blocs, fenêtre B entre les pointes et fenêtre C dans le corps, avec coupures
indépendantes du trajet bas. Masques, accès, fonctions et budgets inchangés.
Couverture 51/120 implémentées, 69 restantes ; 36 720 plans et 204 placements.
Qualification exacte à suivre ; plafond 2 500 inchangé, aucun transfert de
preuves. Pas de PC, installation, fusion, publication ou activation Nether.
Validation visuelle/client non prouvée.

### BP-17 qualifié ; cycle suivant BP-20 A/B/C

BP-17 accepté sur e4380462931e8074b3c5bac05f52eedcfe51917b : 16 jobs verts, 716 JUnit et 117+117
GameTests par pile, streaming/reprise ; 36 720 plans, 204 placements réels,
maximum 2498. JAR non installé :
e40f2f24586ccae8b9f86c55f76fd8d2205795ada5c8964cb366bbb245c019f2.

BP-20 : ceinture facettée large, deux pointes décalées plus étroites ; B tronque
la pointe haute et ajoute un retour, C divise une face basse en deux passages
inégaux. Tests de largeur, retours et coupures séparées. Masques, fonctions,
accès et budgets inchangés. Couverture 54/120 implémentées, 66 restantes ;
38 880 plans et 216 placements. Qualification exacte à suivre sans transfert
de preuves. Plafond 2 500 inchangé ; pas de PC, installation, fusion, publication
ou activation Nether. Validation visuelle/client non prouvée.

### BP-20 qualifié ; cycle suivant BP-34 A/B/C

BP-20 accepté sur 3c853032c01a856c7646a9f5d07511d28cf7f250 : 16 jobs verts, 716 JUnit et 119+119
GameTests par pile, streaming/reprise ; 38 880 plans, 216 placements réels,
maximum 2499. JAR non installé :
3877fbfb495279817340b266c8d0859afc625308f211f4197ad3c2240a5f0330.

BP-34 : deux appuis continus inégaux, tablier montant par escaliers orientés,
passage inférieur distinct ; B fenêtre au niveau bas, C tablier en
deux bandes autour d'une fente. Liaison latérale N3 séparée et masques fixes
préservés. Tests d'appuis, retours, dégagements et coupures indépendantes.
Couverture 57/120 implémentées, 63 restantes ; 41 040 plans et 228 placements.
Qualification exacte à suivre sans transfert de preuves. Plafond 2 500
inchangé ; pas de PC, installation, fusion, publication ou activation Nether.
Validation visuelle/client non prouvée.

Correction BP-34 : 70bca21 dépasse le plafond (2 828 cellules). Les volumes
de vue sont réduits, la fenêtre B garde un contournement bas indépendant,
N3 rejoint le tablier par un court escalier et le puits est dessert seulement
les niveaux médian/haut. Nouvelle qualification exacte requise, sans relever
le plafond ni revendiquer une preuve runtime depuis une estimation statique.

### BP-34 qualifié ; cycle suivant BP-40 A/B/C

BP-34 accepté sur 798da98cb632cb386812a89d3f7a5568cdcc6d28 : 16 jobs verts, 716 JUnit et 121+121
GameTests par pile, streaming/reprise ; 41 040 plans, 228 placements réels,
maximum 2494. JAR non installé :
0f6d60e271bd1639605fa010b9910dce2db37d6e0d2316fddff5a0c76a00b621.

BP-40 : traverse oblique ancrée au sol et au plafond opposé ; B fenêtre
médiane encadrée, C deux attaches hautes depuis un pied unique. Parcours
latéraux soutenus, sans escalade implicite. Tests de continuité des tranches,
retours et coupure isolée de fenêtre. Masques et fonctions conservés.
Couverture 60/120 implémentées, 60 restantes ; 43 200 plans et 240 placements.
Qualification exacte à suivre sans transfert de preuves. Plafond 2 500
inchangé ; pas de PC, installation, fusion, publication ou activation Nether.
Validation visuelle/client non prouvée.

### BP-40 qualifié ; cycle suivant BP-13 A/B/C

BP-40 accepté sur 77c5e5e4f27b2ded8b8f99835fb13dafbb54a295 : 16 jobs verts, 716 JUnit et 123+123
GameTests par pile, streaming/reprise ; 43 200 plans, 240 placements réels,
maximum 2495. JAR non installé :
d9a4800023df92434c1585aa376675a4624a9772b171e5f8d006847b80346602.

BP-13 : nef à trois diaphragmes et fenêtres haute/médiane/basse en série ;
B seconde fenêtre médiane haute, C arche haute dans le dernier diaphragme.
Paliers et échelles soutenus, masques minéraux et fonctions conservés.
Tests de retour, largeur et coupures indépendantes sans raccourci implicite.
Couverture 63/120 implémentées, 57 restantes ; 45 360 plans et 252 placements.
Qualification exacte à suivre sans transfert de preuves. Plafond 2 500
inchangé ; pas de PC, installation, fusion, publication ou activation Nether.
Validation visuelle/client non prouvée.

### BP-13 — correction du témoin après placement DRY

Le candidat ddd4e00 échoue au contrôle de signature BP-34 après placement
réel : le témoin (8,14,6) appartient aux candidats de résidus DRY autorisés.
La signature utilise désormais (9,14,6), dans la même fente mais hors du
rayon de résidus. Géométrie, budgets et récompenses restent inchangés.
La matrice du candidat mesure au plus 2 500 cellules ; cela ne qualifie pas
le nouveau commit. Les quatre CI doivent être exécutées sur la correction.

### BP-13 qualifié ; cycle suivant BP-19 A/B/C

BP-13 accepté sur c493b79867a1d559129c468d6f89096353612463 : 16 jobs verts, 716 JUnit et 125+125
GameTests par pile, streaming/reprise ; 45 360 plans, 252 placements réels,
maximum 2500. JAR non installé :
ba8c3b3e5a905d76331cbf53dc2f8dd0bbbb7a012e8d513a59b5abfb8b10eea5.

BP-19 : deux ailes verticales perpendiculaires et galerie basse ; A arête
vive, B chanfrein médian, C percement diagonal du massif opposé. Les raccords
obliques conservent des sections communes de deux blocs de largeur.
Tests de retours, plans distincts et coupures médianes sans détruire le bas.
Couverture 66/120 implémentées, 54 restantes ; 47 520 plans et 264 placements.
Qualification exacte à suivre sans transfert de preuves. Plafond 2 500
inchangé ; pas de PC, installation, fusion, publication ou activation Nether.
Validation visuelle/client non prouvée.

### BP-19 — marge du plafond composé

Le candidat d381eae mesure 2 503 cellules sur MINER_CAMP/MOTHERLODE,
clé profondeur/variante 577 (BP-17 B). Le nouveau nombre de formes change
les graines couvrant la matrice et expose cette limite dans une forme antérieure.
L'alcôve basse est de BP-17 passe de quatre à trois cellules de largeur,
avec son approche de deux cellules conservée. Masques, passage sous les
pointes, retours, ressources et plafond 2 500 inchangés. La nouvelle CI
exacte doit requalifier l'ensemble ; les échecs précédents ne sont pas acceptés.

### BP-19 qualifié ; cycle suivant BP-21 A/B/C

BP-19 accepté sur 01504b296ed83ab7b46beaf204f3ad82b7d5a0b1 : 16 jobs verts, 716 JUnit et 127+127
GameTests par pile, streaming/reprise ; 47 520 plans, 264 placements réels,
maximum 2500. JAR non installé :
746e363b08a76b8207339056899e171077ca626ede538016e99574753628aabf.

BP-21 : domaines intérieur/extérieur séparés ; A fenêtre haute unique,
B fenêtre basse opposée, C secteur ouvert avec lèvres et passage supérieur
soutenu. Les nodes FINAL restent dehors, sans récompense ajoutée dedans.
Cache sur le cap extérieur, retours extérieurs indépendants des fenêtres.
Tests de séparation, dégagements, lèvres et coupures sans isoler les alcôves.
Couverture 69/120 implémentées, 51 restantes ; 49 680 plans et 276 placements.
Qualification exacte à suivre sans transfert de preuves. Plafond 2 500
inchangé ; pas de PC, installation, fusion, publication ou activation Nether.
Validation visuelle/client non prouvée.

### BP-21 qualifié ; cycle suivant BP-04 A/B/C

BP-21 accepté sur 68ed832a4cb037e86b85ec078094deb729538c1c : 16 jobs verts, 716 JUnit, 129+129
GameTests par pile, streaming/reprise ; maximum 2498.
JAR non installé : b790a7479a18bbe65c197a5d467cebe244271d6f3ebf271d575f0e048918818d.
BP-04 : arcs inégaux autour d'un noyau intact ; raccord intérieur A,
pont oblique B fermant une boucle, deux croissants C et col latéral unique.
Contrats FINAL inchangés, coupures indépendantes et matrice de 51 840 plans,
288 placements. Couverture 72/120, 48 restantes. Qualification exacte à suivre.
Aucun PC, installation, fusion, publication ou Nether. Visuel non prouvé.

### BP-04 — correction de l'ancre de pièce basse

Le candidat 1d1b669 échoue sur le contrat d'ancre ouverte : C utilise une
échelle en (3,3,6), valide pour le parcours mais pas comme centre de pièce AIR.
Le centre bas passe à (4,3,6), cellule AIR soutenue commune aux trois variantes.
Aucune géométrie, masque minéral ou récompense ne change. Qualification à refaire
sur le nouveau commit exact ; les deux tests de parcours dédiés sont conservés.

### BP-04 qualifié ; cycle suivant BP-15 A/B/C

BP-04 accepté sur a76cf03f65e4471c8761669ae27779edc825abb2 : 16 jobs verts, 716 JUnit, 131+131
GameTests par pile et streaming/reprise ; maximum 2499.
JAR non installé : 66ae70cb9248a36b7f2f0a48b1cfbe398eef836fa7d27479fab25413c180bf2a.
BP-15 : col étroit, sections internes pivotantes et bouche évasée ; A lèvres
inégales, B lame avec passage arrière, C auvent et passage inférieur distinct.
Les masques et récompenses restent fixes. Deux tests dédiés et 54 000 plans,
300 placements. Couverture 75/120, 45 restantes ; qualification exacte à suivre.
Aucun PC, installation, fusion, publication ou Nether. Visuel non prouvé.

### BP-15 — largeur de la jonction supérieure

La revue du candidat 217f285 identifie une jonction à un seul bloc entre
la corniche de la cache et l'approche de N3. Le passage supérieur reste
large de deux blocs mais se décale de X=5/6 à X=6/7 ; son raccord arrière
rejoint la même alcôve, sans déplacer N3. Le test dédié contrôle désormais
les deux cellules soutenues sur toute la jonction. Ce constat de revue
n'est pas un échec CI annoncé ; le nouveau source doit être qualifié séparément.

### BP-15 — sonde de test hors du masque N4

Le candidat df94401 passe la matrice composée (maximum 2 496), mais deux
assertions dédiées ciblent (11,3,10), cellule rocheuse réservée au minerai
arrière de N4. La sonde de lèvre droite passe à (11,3,9), hors du masque.
La géométrie et les tests intégrés de tous les fronts minéraux restent
inchangés. Ces résultats partiels ne qualifient pas le successeur : les
quatre exécutions exactes doivent toutes passer après cette correction.

### BP-15 qualifié ; cycle suivant BP-32 A/B/C

BP-15 accepté sur 5a40164910e1a58c50fbff4d27d143e9c7964e75 : 16 jobs verts, 716 JUnit, 133+133
GameTests par pile et streaming/reprise ; maximum 2496.
JAR non installé : 70e511ecb674c8822d9947e604c314be47dc538af136d2202220137b63822ffe.
BP-32 : volute ouverte de rayon décroissant ; A retour par le même chemin,
B fenêtre contrôlée fermant une boucle, C fourche terminale à deux hauteurs.
Lame séparatrice, escaliers orientés et alcôves FINAL conservées. Deux tests
dédiés et 56 160 plans, 312 placements. Couverture 78/120, 42 restantes.
Qualification exacte à suivre. Aucun PC, installation, fusion, publication
ou Nether. Validation visuelle/client non prouvée.

### BP-32 qualifié ; cycle suivant BP-30 A/B/C

BP-32 accepté sur d93870a29346698bb622d7773d8e30fcdb857f29 : 16 jobs verts, 716 JUnit, 135+135
GameTests par pile et streaming/reprise ; maximum 2494.
JAR non installé : 23e1a225c3164bf001f15b111377fa2ada1e05d0952a0487c761992742f496e7.
BP-30 : plafond croissant suivant une diagonale, largeur transversale stable
hors bords biseautés. Les alcôves hautes FINAL restent près du front haut.
A toit continu, B nervure et deux gorges, C galerie et deuxième retour haut.
Tests de profils, dégagements et coupures. 58 320 plans, 324 placements ; 81/120,
39 restantes. Qualification exacte à suivre. Aucun PC, installation, fusion,
publication ou Nether. Validation visuelle/client non prouvée.

### BP-30 qualifié ; cycle suivant BP-25 A/B/C

BP-30 accepté sur e0d4cbbd5b6048f07dfd4b1068ef10b602b0ac92 : 16 jobs verts, 716 JUnit, 137+137
GameTests par pile et streaming/reprise ; maximum 2499.
JAR non installé : 4a0029d01a6936ae344755724012c0114b6edb8bd759c1d49e6b32e2a5bb0e52.
BP-25 : quatre sommets non coplanaires et six arêtes séparées autour de roche
intacte ; B omet BC, C dédouble AD. N6/N7 sont des retraits de BD/CD dans la
normalisation FINAL, sans déplacer de node ni ajouter de récompense.
Tests des six paires isolées et des deux bras, 60 480 plans, 336 placements.
Couverture 84/120, 36 restantes ; qualification exacte à suivre.
Aucun PC, installation, fusion, publication ou Nether. Visuel non prouvé.

### BP-25 qualifié ; cycle suivant BP-08 A/B/C

BP-25 accepté sur dfd349931ec16c82c52fd8b9c7b937775b16971a : 16 jobs verts, 716 JUnit,
139+139 GameTests par pile, streaming/reprise ; maximum 2497.
JAR non installé : 2a28f1fd9c5fe1826aaf383cd897ae1ce3b60008d786317633178f4cbe69d7d9.
BP-08 : voûte fendue, contrefort dominant, puis deux voûtes inégales.
Les coupures isolent leurs deux ouvertures ; masques et récompenses inchangés.
Deux tests dédiés, 62,640 plans et 348 placements.
Couverture 87/120, 33 restantes ; qualification exacte à suivre.
Aucun PC, installation, fusion, publication ou Nether. Visuel non prouvé.

### BP-08 qualifié ; cycle suivant BP-09 A/B/C

BP-08 accepté sur d01bb261b9b40c0d4aacd5c1115b2ebbc6137043 : 16 jobs verts, 716 JUnit,
141+141 GameTests par pile, streaming/reprise ; maximum 2494.
JAR non installé : 6cae4def8272220a2bad32e80b55df7a1ebe015c15ded44295b324de9698e20a.
BP-09 : trois îlots inégaux, toit incliné à deux branches, puis dépression
reliée par escalier. Coupures et retours vérifiés sans modifier les récompenses.
Deux tests dédiés, 64,800 plans et 360 placements.
Couverture 90/120, 30 restantes ; qualification exacte à suivre.
Aucun PC, installation, fusion, publication ou Nether. Visuel non prouvé.

### BP-09 qualifié ; intégration finale des dix familles restantes

Source qualifié BP-09 : 0d272dd81e8b873ff2d393a3a1aec27859b30af9, 16 jobs verts,
716 JUnit et 143+143 GameTests par pile, streaming/reprise. Maximum 2491.
JAR non installé : abafb5659f3c8ac592d25b2d3bb87ea14dc6d14b6860c1c779f3494bc5699b2b.

BP-29/07/39/22/38/28/18/12/33/01 A/B/C sont intégrés ensemble :
120/120 implémentés, mais seulement 90/120 qualifiés avant ce cycle.
Vingt tests dédiés, 86 400 plans et 480 placements réels attendus ;
163+163 GameTests attendus par pile. Qualification exacte du nouveau commit requise,
sans transfert des preuves BP-09. Tous contrats FINAL conservés.
Aucun PC, installation, fusion, publication ni activation Nether. Visuel non prouvé.

### Première CI du lot final — corrections et diagnostic

Le candidat a198a3da15cc6658d1a9603ae2deaa3d64ff54de échoue dans les
quatre workflows. Sept tests signalent des supports d'escalier non explicités,
l'approche de N2 dans BP-22, la signature BP-33, la séparation BP-01 et la stabilité
réelle d'un support posé. Aucun succès final n'est revendiqué.
Les nouvelles pentes ajoutent leurs fondations au plan ; BP-22 déporte son approche
sur Z12/13 pour préserver le minerai canonique X10/Y8/Z11. La signature BP-33
utilise X5/Y8/Z6, hors de la colonne du puits qui rendait l'ancien point ambigu.
Les assertions de séparation et de pose restent strictes et donnent désormais les
positions/états manquants pour diagnostiquer les défauts restants. Nouvelle CI requise.

### Diagnostic précis du second candidat du lot final

Le runtime actuel de 69044c6 conserve cinq échecs. BP-22 et sa largeur passent.
BP-01 A avait écrasé sa marche de retour orientée en ajoutant le passage frontal :
la marche est rétablie après cette composition, sans changer les deux passages.
BP-33 élargit sa première crête à deux cellules, garde deux dépressions et le second
col B ; le dégagement du puits ouest suit désormais la hauteur effective du relief.
Le runtime montre aussi STRAIGHT devenu OUTER_LEFT à X6/Y8/Z10. Les nouvelles
pentes résolvent maintenant leurs angles d'escalier selon les voisins planifiés,
sans lecture du monde ; l'égalité stricte après pose et le rollback restent testés.
Chaque correctif conserve les parcours A/B/C et attend une nouvelle CI exacte.

### Journal des changements natifs pendant la compensation

Le runtime actuel de 5a4d572 termine la matrice des 120 variantes (maximum 2 494)
et ne signale plus que le test de compensation : restaurer un voisin change l'angle
d'un escalier encore possédé, que l'ancienne comparaison prenait alors pour une
édition étrangère. Le rollback capture maintenant les propriétaires par égalité
exacte avant toute écriture. Seul un changement SHAPE observé immédiatement après
sa propre restauration, sur un escalier préalablement égal à son attente, fait
avancer cette attente. Bloc, orientation, moitié et waterlogging restent identiques.
Une modification étrangère préexistante n'entre jamais ensuite dans le journal.
Le journal persiste pour une reprise après échec ; les restaurations restent LIFO.
Un GameTest ciblé vérifie le changement natif et la préservation d'une édition
étrangère portant seulement sur SHAPE, avec répétition idempotente. Total attendu :
164 minimaux + 164 complets par pile. Les assertions existantes restent strictes.
Nouvelle qualification exacte requise ; aucun moteur de génération ou budget modifié.

## État courant — installation confirmée le 16 octobre 2026 à 09:10 UTC

Source runtime qualifiée et installée : `382e0b469358a7c9f32a3988632cd66c770f04c8`,
branche `codex/crystal-cave-contract`, PR64 ouverte en brouillon à la reprise.
SHA256 du JAR : `35feaf9658fb4e0b43ea3710633989f3d49adc88c9e47dbdc5d40d21067b9644`.
Le hash est recalculé sur les octets conservés dans ce workspace ; le constat
PC ci-dessous est transmis par le fil propriétaire, sans accès PC depuis ce cycle.

L’installateur approuvé a installé ces octets à **09:10 UTC** dans Prism
**1.21.1 TesT play 2** : un seul fournisseur IOE actif ; ancien JAR `dc3bfa7`
(SHA256 `60bf6e86e0aaa6df75612d388c4e8a1e79cf0a8f2439b66bc1437a89af85261e`)
sauvegardé ; **148 autres mods et 243 configurations inchangés**.
Génération naturelle TRUE et retrogen FALSE préservés. **Aucun monde lancé**.
Installation terminée : ne pas la répéter. Aucun résultat client/visuel n’en découle.

Relecture distante : quatre runs réussis, **16 jobs verts**, tous liés au head
runtime ci-dessus : baseline [push 38874735369](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/actions/runs/38874735369)
et [PR 38874741675](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/actions/runs/38874741675),
versions actuelles [push 38874735466, tentative 2](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/actions/runs/38874735466/attempts/2)
et [PR 38874741658](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/actions/runs/38874741658).
Chaque pile : 716 JUnit sans échec/erreur/ignoré, 78 GameTests minimaux et 78 complets,
plus streaming et redémarrage séparés. JAR : 467 classes et métadonnées vérifiées.

### Prochain écart essentiel, sans nouvelle fonctionnalité inventée

La qualification client du **candidat déjà installé** reste NOT_PERFORMED :
chargement avec journal frais, rendu/interactions Jade, progression SITE_LOCATED
et manuel, sauvegarde/sortie/reprise. Voir le [runbook actuel](SMOKE_VALIDATION.md).
La présente reprise n’autorise aucune opération sur les mondes utilisateur, aucun
nouveau serveur/profil ou installation ; un responsable et un scénario de monde
explicitement autorisé sont nécessaires avant cette observation. Ne pas convertir
les succès CI en preuve client, ni relancer les études Nether déjà clôturées.

La revue bornée de la feuille de route, des contrats et du gate source n’a pas
identifié de nouvelle régression de code démontrée à corriger dans ce cycle.
Les décisions de produits Fluix/Draconium et budgets de cave restent ouvertes dans
CRYSTAL_CAVE_CONTRACT.md ; elles ne sont pas une autorisation d’implémentation.
Nether conserve 74×74, LOWEST de la colonne candidate sans fallback, budgets partagés,
read set/revalidation, nouveaux chunks uniquement, aucun chargement forcé,
déduplication persistante et compensation best-effort ROLLBACK_INCOMPLETE.
`NetherWorldActivation.PRODUCTION_ENABLED=false` reste inchangé ; activation,
fusion PR64 et publication nécessitent leurs décisions distinctes.

Ce changement est un **successeur documentaire**, pas un nouveau candidat runtime.
Aucun code, dépendance, configuration, datapack ou octet du JAR installé n’est modifié.
Validation documentaire ciblée uniquement, CI de ce successeur NOT_PERFORMED
(`[skip ci]` pour ne pas répéter la qualification achevée). Son commit et le reçu
distant sont résolus dans Git/PR64 ; les 16 succès restent attribués à `382e0b4`.

## Correction de portée d'activation — 16 octobre 2026

Les mentions globales « Production OFF » des bilans PR64 étaient incorrectes.
Au source `dc3bfa7a349f8f2267c0c3799eb4820096f35057`, les sites Overworld sont
activés par défaut (`worldgen.global.naturalExpeditionSiteGenerationEnabled=true`).
Le proof hook ancien reste séparément désactivé ; le routage de production Nether
reste fermé. Le garde `IoeNewChunkOreGuard` peut modifier les ressources des nouveaux
chunks indépendamment du gate des sites. Voir [portées et exemples corrigés](SERVER_CONFIG_EXAMPLES.md).

Cela préserve la décision utilisateur du 14 octobre : « Active IOE dans les nouveaux
mondes puis fusionne PR63 après les tests ». Ni nouvelle activation Nether ni
permission de fusion PR64 n'est déduite. Ce successeur documentaire ne change aucun
default, switch, code ou datapack ; tous les contrats Nether 74×74, budgets partagés,
read sets/revalidation, déduplication et ROLLBACK_INCOMPLETE sans réparation des
anciens chunks sont conservés.

Preuve transmise par le fil propriétaire : le JAR de `dc3bfa7` a été récupéré et
haché indépendamment sur PC, SHA256
`60bf6e86e0aaa6df75612d388c4e8a1e79cf0a8f2439b66bc1437a89af85261e`.
Ce hash est attribué à cette source, pas au présent successeur documentaire ;
il n'a pas été recalculé dans ce workspace cloud. Ce JAR a été installé à 06:25 UTC
avec GeOre 6.2.4, puis sauvegardé lors du remplacement autorisé de 09:10 UTC décrit
ci-dessus. L'ancien motif « GeOre absent / installation suspendue » est périmé.
Qualification client/visuelle toujours non prouvée.

La CI de ce successeur et le reçu distant exact sont consignés dans PR64 ; les
14 checks verts de `dc3bfa7` restent identifiés séparément.

## Référence technique vérifiée — 2026-10-10 UTC

Ce relevé est le point de reprise technique commun aux interfaces, dans le document
existant déjà référencé par le README. Il applique le workflow explicitement approuvé
par le propriétaire : « Ok, j'aimerais ça que tout le projet que je t'ai envoyé suive
cette règle de développement ». La gouvernance conserve ses propres responsabilités ;
aucun nouveau schéma, service de synchronisation ou registre concurrent n'est créé.

### Identités séparées

| Élément | Référence vérifiée |
| --- | --- |
| Dépôt / module | `emmanueltremblay9-stack/Immersive_Ore_Expedition` / `ioe_project_packs/immersive_ore_expedition` |
| Branche de reprise | `codex/canonical-budding-site-planner` ; [PR63](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/63), ouverte en brouillon |
| Commit source qualifié | `46051a13febd676ef59d21d98dd6528b05915727` |
| Arbre source | `ab0ee71867b7670f00643ce16ee21fcf261f6849` |
| Checkout CI PR | `8b153ea1ea7fcfca2a0e0833454eae7b8ed1955f` ; même arbre que le commit source |
| État local avant cette mise à jour | HEAD et branche distante égaux au commit source ; aucun changement suivi ou non suivi |
| Commit du présent état | À résoudre dans l'historique Git de ce fichier et le reçu distant de la session ; volontairement distinct du commit source ci-dessus |
| Version du JAR | `0.2.50-alpha`, aucune release publiée par ce cycle |

Le commit contenant ce relevé est un successeur documentaire. Il ne prétend pas être
le commit ayant produit le JAR. Pour retrouver la dernière modification du relevé,
consulter `git log -1 --format=%H -- ioe_project_packs/immersive_ore_expedition/docs/PATH_TO_3_0.md`,
puis vérifier ce commit et le fichier à cette référence sur GitHub. L'état final du
push ne peut être prouvé avant son exécution : le reçu de fin de session/PR fournit
cette preuve externe, sans boucle de réécriture du hash dans le document.

### Preuves liées exclusivement au commit source

- [CI push 38023797062](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/actions/runs/38023797062), tentative 1 : succès.
- [CI PR 38023800457](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/actions/runs/38023800457), tentative 1 : succès.
- 14 jobs verts au total ; chaque exécution : 680 JUnit, zéro échec/erreur/ignoré,
  72 GameTests requis en configuration de base et 72 en configuration complète épinglée.
- [Artefact runtime 11658996934](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/actions/runs/38023797062/artifacts/11658996934), rattaché au commit source ; non expiré à la vérification.
- SHA256 de l'archive d'artefact : `10faab65ad78ecc5b7cafb7ad7f3dcdc7f601854fbb268e5d57dd81c4734fc78`.
- JAR : `immersive_ore_expedition-0.2.50-alpha-neoforge-1.21.1.jar`, **2 461 306 octets**.
- SHA256 du JAR : `040ffc4fb9b9764f5f3e9d3276ebeb0044ed522a93936c68ea0e7dbe9cfd9c2e`.
- Inspection : 445 classes IOE, `META-INF/neoforge.mods.toml`, méthodes compilées
  d'observation d'entrée, 28 fichiers du manuel et langues EN/FR conformes aux sources.
  Empreinte du JAR recalculée avant cette mutation documentaire.

L'expiration future d'un artefact doit être signalée ; un nouveau build ne remplace
pas silencieusement cette identité. Les éventuelles CI du successeur documentaire
ont leur propre commit et ne changent ni ces résultats ni ce JAR.

### Implémentation, tests, propositions et acceptation

| Dimension | État |
| --- | --- |
| Décision propriétaire | Entrée extérieure approuvée pour `SITE_LOCATED` ; workflow Git transversal approuvé |
| Implémenté | Indice visible puis entrée extérieure réellement observée à huit blocs maximum ; progression personnelle persistante, dédupliquée et bornée ; aucun dévoilement de chambre, ressource ou qualité ; fonctionnement DRY sans outil/IE/Jade requis |
| Testé automatiquement | Preuves exactes ci-dessus ; signatures des quatre types de surface et cinq qualités ; scénario de progression, refus, deux joueurs et sauvegarde/relecture SavedData |
| Restant à décider / non implémenté | Preuves admissibles pour `RESOURCE_IDENTIFIED`, vérification de `SITE_SURVEYED`, déclencheur de `EXPEDITION_DOCUMENTED` |
| Contenu manuel | Neuf entrées narratives statiques EN/FR et consultation personnelle ; autres chapitres/atlas incomplets, distincts des producteurs de gameplay |
| Qualification runtime/client | `NOT_PERFORMED` pour exploration réelle, rendu/interaction client, reconnexion et redémarrage complet ; tests automatiques non substitutifs |
| Acceptation finale | Non acquise ; approbation du comportement demandé ne vaut pas acceptation visuelle, stabilité 1.0.0/3.0, merge ou publication |
| Gates | PR63 brouillon ; aucun merge/release/installation/activation Nether autorisé par ce relevé ; gates de production inchangés |

### Reprise et clôture de session

Avant toute mutation, confronter ce relevé à la branche/HEAD distants, au checkout
local, à `git status --short` (y compris les fichiers non suivis), aux contrats et
au hash de l'artefact utilisé. Préserver le travail existant. Une différence impose
une réconciliation de provenance ; ne pas réinitialiser, écraser ou repartir de main
obsolète. Une progression documentaire seule est explicable par son diff ; une
modification du candidat exige de nouvelles preuves propres à sa source.

À la clôture, enregistrer les changements autorisés et les preuves pertinentes dans
ce relevé, pousser sur la branche existante, relire le ref/commit et les octets des
fichiers distants, puis rapporter `VERIFIED`, `PERSISTED_UNVERIFIED`, `PARTIAL` ou
`FAILED` selon le résultat réel. Distinguer le commit source, celui portant l'état,
les éventuels fichiers restés locaux et les décisions encore proposées.

### Frontière du dépôt Gov

Lecture distante : main Gov `2257fc3db7b296aa055bb1548955767da0446c7f` ; PR6 ouverte
en brouillon à `7d01e0b264f26b6be76a0fa012cb3d78389ea6c9` sur
`governance/nether-ore-biome-proposals`. Le handoff Discovery approuvé est intégré à
main Gov. `governance/CURRENT_STATE.md` reste l'instantané historique d'adoption du
2026-10-07 (blob `d1e4c631a24ca58a521d4df625f104974992884e`) ; ne pas le présenter
comme le head technique actuel. Aucun fichier Gov n'est modifié dans ce cycle limité
à PR63. Son actualisation globale et le routage de ce relevé technique relèvent d'un
lot Gov distinct ; ils ne doivent pas être insérés silencieusement dans le dossier
Nether proposé de PR6. Aucune décision Nether/IPM n'est prise ici.

## Suivi borné Nether — 2026-10-12

Une unique recherche supplémentaire autorisée a examiné 256 nouvelles régions, sans
modifier les critères : **zéro acceptation et zéro écriture de géode naturelle**.
Le commit expérimental est `a0e7452239cb64b0fc00a0ef5be9881c1471a8c0` ; CI push
`38192711455` et PR `38192716620` réussies, mais seule la configuration de base du
push exécute la recherche. Le chargement 5x5 reste artificiel, non représentatif.
Voir `NETHER_BOUNDED256_EVIDENCE.json` (256 traces) et la clôture détaillée dans
`NETHER_PLACEMENT_CONTRACT.md`. Plafond atteint : aucune nouvelle boucle lancée.

L'instrumentation temporaire est retirée ; les fichiers de code retrouvent exactement
le checkpoint `5d14b64366c6ef4da080e1ea1494b00787cef002`. La source gameplay et le JAR
qualifiés ci-dessus restent inchangés. Le commit de restauration/documentation est
à lire dans Git ; il utilise `[skip ci]` pour ne pas répéter la recherche historique.
Ne lui attribuer ni les tests du commit expérimental ni une nouvelle qualification
naturelle. CI de cette restauration : `NOT_PERFORMED`. Production désactivée.

## Comparaison de géométrie — 2026-10-12

Étude autorisée, distincte d'une adoption : 32 terrains naturels appariés, quatre
variantes profondeur/cube (16/15, 12/15, 8/15, 12/13), **zéro plan admissible pour
chacune**. Les 128 évaluations et read sets sont conservés dans
`NETHER_GEOMETRY_COMPARISON_EVIDENCE.json`; méthode, limites et recommandation dans
`NETHER_GEOMETRY_COMPARISON.md`. Source expérimentale
`bd6849ef08c1cdc6ba636dea4b15f748e25f2917`, CI push `38201536790` et PR `38201539982`
réussies. Une seule exécution de la comparaison, en baseline ; aucune écriture.

Recommandation provisoire si refonte approuvée : profondeur 12 avec cube 15, mais
aucun gain empirique d'admissibilité démontré. Les blocages dominants précèdent cette
étape (couverture/fond). Aucune modification du contrat de production n'est adoptée.
Instrumentation retirée : code identique au checkpoint `61f6f1c...`; la preuve CI de
l'expérience ne se transfère pas au commit documentaire de clôture. La validation
normale de cette clôture sera consignée par reçu exact sur PR63, sans refaire l'étude.

## Fonds magma / sable des âmes — approbation et implémentation du 2026-10-12

Le propriétaire a approuvé uniquement `magma_block` et `soul_sand` comme premiers
fonds observés du lac. Le prédicat est mis à jour avec tests de non-remplacement,
revalidation, exclusions et absence de bonus `soul_soil`. Profondeur F-16, cube 15,
225 cellules, fenêtre 74x74, LOWEST et gates de production restent inchangés.
Voir l'amendement de `NETHER_PLACEMENT_CONTRACT.md` pour la portée exacte.

Nouvelle source : commit portant cette modification de `NetherSitePlanner.java`,
à résoudre par `git log -1 --format=%H -- ioe_project_packs/immersive_ore_expedition/src/main/java/com/oblixorprime/ioe/nethergeodes/NetherSitePlanner.java`.
Elle est distincte des anciennes sources/JAR qualifiés ci-dessus. La comparaison
réutilise les mêmes 32 terrains via le GameTest existant et ne change aucune géométrie.
Le reçu de clôture sur PR63 lie les résultats réellement mesurés, CI et JAR à ce
nouveau SHA ; ne pas transférer les preuves précédentes ni déduire une acceptation
naturelle du seul succès CI. Ce document est un état d'implémentation, pas une
acceptation de stabilité, de rendu ou de production.

## Historique de consolidation et contrats conservés

Les sections suivantes conservent leur contexte de cycle. Les expressions « ce
cycle » et les anciennes suites ne remplacent pas le relevé daté ci-dessus.

Le numéro 3.0 est un objectif demandé, pas une spécification de fonctionnalités,
une hausse de version autorisée ou une preuve de stabilité. Le module actif reste
`immersive_ore_expedition`, version inchangée. Les roadmaps des modules séparés et
`00_master_planning_pack/PHASE_PLAN.md` servent d'index historique ; les contrats
actuels prévalent sur leurs anciens jalons « remaining ».

| Lot | Autorisation / état actuel | Suite déterminée |
| --- | --- | --- |
| Boucle Overworld, provinces, indices et camps | Périmètre existant, worldgen naturel actif ; les roadmaps historiques ne décrivent plus précisément son état | Corriger les écarts démontrés de production, puis qualifier le candidat exact |
| Budding GeOre et Certus | Contrats canoniques actuels ; budgets/rangs, métadonnées et persistance déjà raccordés | Préserver ces règles et leurs régressions ; aucune nouvelle famille inventée |
| Météorites AE2 | Contrat parallèle : AE2 conserve ses météorites et sa progression normale | Ne pas réactiver l'ancienne proposition de reproduire des météorites dans les sites IOE |
| Nether 74x74 | Taille et gameplay approuvés ; lecture complète/revalidation implémentées ; génération bloquée | Dépend d'un backend de publication qualifié ; ne pas remplacer ce prérequis par davantage de diagnostics |
| Retrogen/admin | Outils et protections existants ; mutation par défaut désactivée | Aucune activation implicite ou extension de retrogen |
| Claims externes | Aucun fournisseur choisi dans le périmètre épinglé | Aucune compatibilité de claims annoncée ou nouvelle dépendance spéculative |

## Incrément indépendant de ce cycle

Le contrôle de collision des camps utilisait le `StructureManager` du ServerLevel
pendant une Feature. Dans Minecraft 1.21.1, `StructureManager.startsForStructure`
(lignes mappées 51-61) demande le chunk à STRUCTURE_REFERENCES via le LevelAccessor ;
`fillStartsForStructure` (77-84) demande les chunks d'origine à STRUCTURE_STARTS.
Cela contourne le contexte de dépendances de la région en cours de génération.
Les artefacts épinglés sont ceux de `NETHER_PIPELINE_AUDIT_EVIDENCE.json` ; cette
observation concerne un chemin Overworld actif, sans réouvrir le backend Nether.

Le contrôle utilise maintenant exclusivement :

- les chunks déjà chargés via `getChunkNow` pour ServerLevel ;
- le cache de dépendances du WorldGenRegion, au statut demandé, pour la génération ;
- les starts locaux et références vers leurs chunks d'origine, sans appel au
  StructureManager serveur et sans demande supplémentaire de génération.

Une collision, un chunk/statut/start référencé indisponible ou la limite de travail
atteinte refuse le camp avant placement. Les mêmes boîtes englobantes avec marge
existante sont conservées. Le contrôle est borné à 64 chunks d'emprise et 256 entrées
métadonnées (starts, groupes de références et références), soit au plus 320 consultations
de chunks et 256 starts retenus par invocation. Chaque fallback est contrôlé séparément ;
le nombre de qualités reste borné par les cinq qualités existantes. Aucune lecture de
blocs, file d'attente, ticket ou copie persistante des métadonnées n'est ajoutée.

Les GameTests vérifient le refus sans chargement d'une emprise inconnue, une collision
référencée, le changement des métadonnées, un start invalide/absent et l'absence de
mutation de blocs. La consultation ServerLevel est exercée sur de vrais chunks.
Le chemin WorldGenRegion repose sur l'API épinglée inspectée ; les tests ne constituent
pas une preuve de rendu client ni une qualification exhaustive de la génération naturelle.

Limites : seules les structures enregistrées dans les métadonnées natives sont
couvertes ; pas les constructions de joueurs ni les claims tiers. Des métadonnées
incomplètes peuvent désormais refuser un site auparavant admis/chargé implicitement.
Ce correctif ne généralise pas la protection à tous les types de sites, ne raccorde
pas les collisions Nether et ne résout pas la publication atomique.

### Revalidation à l'application différée

Un GameTest ajouté sur `97ef53af5916f9eedda2ebc1366ee696de42cfb4` a reproduit
l'acceptation d'un camp malgré une référence de structure ajoutée après staging
(CI PR 38006487382 : seul échec, `Deferred camp accepted a structure added after staging`).
Le bloc initial reste de l'air : les contrôles de matériau, fluide et block entity ne
peuvent pas détecter cette modification de protection.

`IoeExpeditionPlanPlacement.apply` réutilise désormais le même contrôle avant toute
lecture/écriture de terrain, pour chaque plan MINER_CAMP, principal ou fallback.
La marge d'un bloc, les limites 64 chunks/256 entrées, la lecture des seuls chunks
disponibles et le refus en cas de métadonnées inconnues sont conservés. Les tests
vérifient également un start déplacé, invalide ou devenu indisponible, la reprise
du placement après retrait du conflit, l'absence de chargement forcé et le maintien
de la portée limitée aux camps. Il s'agit d'une mutation de métadonnées injectée
dans un ServerLevel réel, pas d'une mesure de fréquence en génération naturelle.

## Décision de périmètre approuvée et suites restantes

Les anciennes roadmaps mentionnent dangers Nether, nouvelles variantes/branches de
mines et retrogen élargi. Ces intitulés ne définissent ni règles de gameplay ni
critères d'acceptation suffisamment précis pour les ajouter au titre de « 3.0 ».

Le 2026-10-09, l'utilisateur a choisi explicitement l'option 1 : **consolider le
périmètre actuel et reporter la génération Nether**. Cette décision remplace la
proposition de périmètre précédente. La génération Nether n'est donc pas un
critère de livraison de cette consolidation. Le travail 74x74, les budgets, le
coordinateur, les diagnostics et les tests sont conservés pour une reprise future ;
ils ne constituent pas une fonctionnalité de génération livrée.

Le verrou reste inchangé : `NetherPlacementRuntime.commit` retourne
`BACKEND_UNVERIFIED`, `SubLavaGeodeGenerator.generateBelowLake` retourne `false`
et aucun chemin automatique de publication n'est enregistré. Les options de
planification ne lèvent pas ce verrou. Sa levée nécessitera une qualification
technique distincte et une décision explicite de reprise du périmètre Nether.

La consolidation couvre les fonctionnalités déjà implémentées et approuvées
(Overworld, Budding GeOre/Certus, AE2, Jade et persistance), sans nouveaux dangers,
familles, variantes, claims ou retrogen. La version reste `0.2.50-alpha` et les
notes restent `NOT_PUBLISHED` ; 3.0 est une cible, pas une release réalisée.

### PR62, traitement séparé proposé

PR62 (`codex/test-budding-rank-restoration`, head
`239450e75a5095d2d27845cc79c8e0ca0db7b420`) reste ouverte en brouillon. Elle ajoute
seulement trois tests de plafond de restauration dans `BuddingRankTest.java` ; ce
fichier n'est pas dans PR63 à ce checkpoint. Aucune fusion ni clôture n'est décidée.
Proposition : revoir séparément son utilité par rapport à la couverture actuelle,
puis décider soit de la conserver pour intégration ultérieure, soit de la clore
explicitement si jugée redondante. Ne pas la présenter comme déjà intégrée.

### Prochaines étapes réelles

1. Lier la CI et, si accessibles, les octets du JAR au head final de ce cycle dans
   un relevé externe ; les preuves historiques restent propres à leurs commits.
2. Choisir un environnement autorisé et un responsable pour les validations
   client, Jade, progression, save/exit/reopen et serveur. Aucune installation
   n'est autorisée par cette décision ; ces validations restent non réalisées.
3. Décider séparément du traitement de PR62, puis des autorisations de revue,
   fusion et publication une fois les critères satisfaits. Rien ne renomme
   automatiquement le candidat en 1.0.0 ou 3.0.

Pré-requis de publication toujours distincts : CI et JAR au commit exact, validation
manuelle client/visuelle non prouvée, qualification serveur non réalisée dans ce
cycle (installation refusée), puis autorisation explicite de merge/publication.
Aucune de ces validations n'est remplacée par des tests automatiques. Ce cycle ne
modifie ni le numéro de version ni les autorisations de release.

## Nether implementation resumed, activation still deferred

After the consolidation decision, the owner requested renewed Nether development
and on 2026-10-09 at 19:14:50 UTC approved best-effort compensation with durable
`ROLLBACK_INCOMPLETE` and no retry/old-chunk repair. Only atomic all-or-nothing
placement is relaxed; the 74x74 gameplay and other guarantees remain. The prepared
placement backend is now wired to real world SavedData, behind the unchanged
automatic generation gate. Consolidated Overworld scope and version are preserved.
See `NETHER_PLACEMENT_CONTRACT.md` for the exact amendment and test boundaries.

## Authorized coverage experiment — 2026-10-13

The owner explicitly approved testing 40% lake coverage (2,191 of 5,476 columns),
not adopting or activating production. The normal planner entry remains at 60%;
a package-scoped explicit experimental argument changes only coverage. The existing
32-region hosted observation compares 60% and 40% on the same captured terrain,
then revalidates the complete union read set with the same shared budget.
74x74, LOWEST/no fallback, F-16, all 225 floor cells, the 15-cube protection volume,
materials, rewards, new-chunk authority and deduplication are unchanged.
No experimental placement is performed: PLANNED is distinct from accepted/committed.
The experiment follows every downstream planner check until rejection or PLANNED;
exact CI results and per-candidate transitions belong in the PR63 execution receipt.
No claim of natural success, player-stream qualification or client validation follows
from automated success. Production remains disabled; no merge/release authorized.

## Targeted volume rejection diagnosis — 2026-10-13

Investigation of the already recorded candidate (-512472,-511864) adds the exact
first rejected volume cell to the planner outcome, using the state already read.
No extra terrain read, predicate change, search, protection relaxation or placement
is introduced. The existing hosted cohort now logs this target's center, position,
offset, state and each rock-predicate condition after complete read-set revalidation.
This supplies the missing cause; repeated cohort results are not a new eligibility
experiment. Exact-source CI and the observed cause are recorded in the PR63 receipt.
Normal coverage remains 60%, explicit experiment 40%, production disabled.

## Approved intact-bedrock contact trial — 2026-10-13

Owner explicitly permits exterior bedrock contact only where bedrock stays intact.
The planner accepts unprotected, fluid-free, block-entity-free bedrock in retained
volume cells. Every cavity cell still requires replaceable rock. The full radius-5
shell remains in the original deterministic shuffle; if a selected resource write
hits bedrock, reject the whole plan. No skipping, reroll, truncation or replacement.
Unselected shell cells and outer envelope can retain bedrock; it remains in the
complete state/protection read set. Runtime replacement permissions are unchanged.
Bedrock remains invalid as observed lake floor or shore. All other criteria remain:
74x74, LOWEST, F-16, 225 floor cells, full 15-cube, rewards, budgets and new chunks.
The complete outer top is already below selected lava surface by this geometry;
no new margin is added. Normal coverage remains 60%, the explicit experiment 40%.
The existing targeted observation records the next outcome without forcing a plan
or placing terrain. Exact-source CI and findings are recorded on PR63. Production
remains disabled; no merge, release or installation is authorized.

## Approved native substrate palette — 2026-10-13

The owner explicitly permits the seven discussed natural substrates as observed
floors, retained volume, and inputs to the existing cavity/resource writes:
`netherrack`, `magma_block`, `gravel`, `blackstone`, `basalt`, `soul_sand`, `soul_soil`.
This is an exact block list, not a natural-block tag or permission for ores, rewards,
structures, player work, block entities or protected cells. Original shore palette
remains unchanged. Bedrock retains only its previously approved read-only exception.

Official Minecraft 1.21.1 server data was downloaded for inspection only, not run or
installed. SHA1 of the official bundle: `59353fb40c36d304f2035d51e7d6e6baa98dc05c`.
See `NETHER_NATIVE_PALETTE_EVIDENCE.json` for Mojang URL, hashes and biome features.
Default terrain is netherrack. Basalt deltas surface rules select basalt/blackstone
and gravel patches; soul-sand valleys select soul sand/soil and gravel patches.
Nether wastes include soul-sand/gravel layers. All five Nether biomes include magma
features; the four non-delta biomes also include gravel/blackstone ore-shaped deposits
(these configured-feature names generate gravel/blackstone, not reward ores).
Delta blobs/columns and valley basalt pillars add biome-specific basalt. Forest
nylium/wart surface materials exist but are outside the approved list. This palette
is not a promise that every lake bottom has all seven materials.

Retained gravel is checked against final planned states using vanilla
`FallingBlock.isFree`. A gravel stack is followed down to a non-gravel support,
including outside the inspected cube when necessary. An unsupported final stack
rejects the plan with GRAVEL_SUPPORT; no invented support or shape truncation.
All support reads use the same bounded Reader, loaded-only capture and revalidated
read set. Replaced gravel follows the unchanged exact write selection. Bedrock is
never written. F-16/radii and therefore the entire exterior-top guarantee remain.

The existing 32-region hosted comparison retains 60%/40% paired planning. If the
40% plan is admissible, the fixture uses real new-chunk receipts and the existing
prepared backend/real ledger in the same tick. Commit performs full revalidation;
no extra manual full reread is charged beforehand for these plans. Rejected plans
still receive diagnostic reread. No receipt injection, budget reset, retry or added
region. Twenty ticks later the fixture checks writes and retained gravel/bedrock.
Controlled FULL 5x5 loading remains artificial. A separate small GameTest verifies
vanilla falling/stable gravel behavior. Exact CI and actual placements belong in
the PR63 receipt, not inferred from PLANNED. Production remains disabled; no new
server/profile, merge, release or installation is authorized.

## Approved new-world depth32 prototype — 2026-10-13

Owner selected 32 additional blocks downward, retaining lava at Y=32. External
`prototype/depth32` data sets minY=-32, build upper boundary 256 exclusive, generated
upper boundary 128 exclusive and logical upper boundary 128. Lower bedrock moves
32 blocks down; roof anchors remain at their original upper boundary. Existing
noise density gradients are retained. Cave centers and cave-lava threshold, fossil
heights and relative Nether feature ranges are frozen to original absolute heights.
This avoids silently moving existing ore/decorative ranges; it does not promise
identical terrain because random consumption and carver geometry can differ.

Shared placed features are cloned under `ioe_depth32` and referenced only by five
vanilla Nether biome definitions. Suppressed mineral clones join the existing IOE
removal tag. Overworld placed features and production data are not overwritten.
The normal JAR excludes the entire external pack, checked by CI. Loader activation
requires an explicit property, development GameTest mode and a directory without
any level.dat/level.dat_old/region file; it refuses existing worlds without deletion
or migration. Only the two existing hosted GameTest profiles enable this prototype.
No production activation or new profile is introduced.

Existing bounded 32/64 cohorts exercise the new depth without altering LOWEST,
74x74, 60% normal/40% experiment, F-16, exact writes/rewards, protections, budgets,
full read-set, new-chunk receipts or deduplication. Runtime checks observe negative
substrate, bedrock floor/roof, lava/build/logical bounds and portal creation; static
checks cover anchors and mineral suppression. Pinned full runtime is the measured
compatibility scope; other mods' relative anchors/custom dimensions remain unqualified.
Fresh-world checks do not constitute an existing-save migration test. Exact-source
CI, artifact inspection and measured placement outcomes are recorded in PR63's
receipt. Visual client validation and a complete process restart remain unproven.

The first prototype runtime reached a natural commit, exposing a fixture-only unload
before its delayed check. The cohort now retains its existing FULL 5x5 through a
radius-2 test ticket for those 20 ticks (center block-ticking), then removes it.
No additional FULL neighborhood or production ticket is introduced. Successful
cohort commits additionally save/reload their real ledger and verify provenance
and replay refusal, logging negative-height writes separately. This is SavedData
roundtrip evidence, not a complete server restart.

## Normal 60% admission on the known depth32 candidate — 2026-10-13

The (-2014,-2000) cohort entry now calls `NetherNaturalAdmission.attemptDetailed`
at the unchanged normal 60% threshold, replacing its paired planning and prepared
40% commit. It does not add a second placement, retry, region, server or profile.
The other 31 entries keep their previous comparison and are separate evidence.
A third full capture after paired planning would exceed the existing shared 65536
reads/tick; instead this entry uses the existing charged necessary-condition filter
and charged 15-cube observations before/after the real admission. No budget reset,
extra tick before admission, receipt injection or terrain preparation is introduced.

Counters distinguish admission capture reads, prefilter reads, before/after volume
reads and delayed volume reads. Commit's protected full revalidation still uses the
existing reservation and checks; no independent actual commit-read counter exists,
so the log explicitly says not_instrumented rather than claiming a total. The full
pinned depth32 fixture must actually COMMIT; a mere PLANNED or rejection cannot pass.
It checks observed changed blocks against the report, cavity/resource/debris budgets,
no bedrock changes, the whole volume after 20 ticks, real SavedData disk roundtrip,
resource provenance and replay refusal. Loading and temporary retention are controlled
FULL 5x5 fixture operations, not player streaming. Ledger reload is not chunk reload
or a complete process restart. Prototype data remains outside the normal JAR;
production, merge and publication remain disabled. Exact-source results belong in
the PR receipt after hosted CI finishes.

The first full-runtime normal-admission attempt reached COMMITTED but exposed a
fixture assertion that incorrectly equated observed cube changes with every plan
write. The unchanged plan also writes two shore-marker blocks outside that cube.
The fixture now checks cube changes against committed plan writes minus those two
markers and logs these quantities separately. Shore writes remain covered by the
coordinator's successful commit checks, not an independent fixture before/after
observation; the 20-tick volume check covers the cube only. No gameplay fix was made.

## Real chunk unload/reload qualification — 2026-10-13

The already successful normal-60% candidate now continues in the same GameTest,
without another placement or new profile: save/flush chunks through the public
vanilla API, release its existing temporary ticket, observe the center chunk absent
for five consecutive ticks (200-tick bound), then perform one controlled reload.
The fixture requires a different LevelChunk object, no new-generation receipt,
all 3375 cube states still equal after 20 further ticks, retained resource provenance
and duplicate admission refusal. Every verification block read debits the existing
shared budget. It never edits unload internals, injects events or relaxes freshness.
The cube must be wholly in that chunk; its two shore markers remain outside this
reload observation. Log counts for shore writes now report zero for rejected plans.

This distinguishes prior SavedData-only roundtrip from observed chunk absence,
new object and persisted block states. It is not a process restart, player-driven
streaming, visual validation, existing-world migration or production qualification.
Only the known committed candidate gains this check; baseline rejection adds no
reload trial. Production and the external-pack exclusion remain unchanged. Hosted
CI at the resulting source must supply the actual outcome; this entry is not itself
an assertion that the unload/reload passed.

Reload freshness is checked one tick after loading, inside the existing 20-tick
lease window, as well as after the delayed volume verification. Checking only at
20 ticks would allow an erroneously fresh receipt to expire before observation;
the early assertion prevents that false proof without changing lease semantics.

## Include both shore markers in the same reload proof — 2026-10-13

The normal admission report now includes an immutable, package-private list of the
shore positions from the successfully committed plan (empty on every noncommit).
This is read-only result metadata; it adds no terrain scan, permission or generation
change. The existing known-candidate fixture asserts exactly two adjacent vertical
blackstone markers outside the cube and inside its already loaded FULL 5x5, then
reads them with the shared budget and includes them in the 20-tick checks.

The same save/unload/reload cycle now waits for ALL unique cube/marker chunks to be
absent together for five ticks, reloads each once, requires each object to differ,
and checks freshness at tick 1 and all 3377 observed states at tick 20. No extra
cohort, study, placement, profile or neighborhood. Marker observations start after
commit; their expected before-state remains the planner's validated read set, not
an independent fixture before-observation. CI must establish the actual chunk count,
positions and outcome at the resulting source.

### Complete process restart boundary

The existing two Gradle profiles are development GameTest servers in
`run/gametest-baseline` and `run/gametest-full`. NetherDepth32Prototype.register
calls requireFreshDirectory before registering the external pack, rejecting any
level.dat, level.dat_old or .mca. Therefore relaunching either profile against the
saved prototype world fails intentionally. Cleaning the world and rerunning tests
would only test a new world, not persistence. Disabling the prototype or bypassing
this guard would invalidate the requested proof or widen the new-world-only scope.
A second invocation could reuse the existing Gradle profile, but the saved-world
refusal still prevents that proof; a fresh run cannot stand in for a restart. No
restart, resume exception, guard bypass, extra profile or access expansion is
implemented. A future restart qualification needs explicit approval for a bounded
exception reopening only the proven CI fixture world in the same profile. It cannot
be inferred from chunk reload or SavedData tests.

## Owner-approved same-profile CI process restart — 2026-10-13

Explicit owner approval permits reopening only this CI execution's own fresh
prototype32 fixture, never an old player or unknown save. Default behavior still
calls requireFreshDirectory unchanged. The optional full-profile fresh/resume
phases require GitHub Actions identity, exact module/profile path, run/attempt/SHA
and a per-execution nonce. Fresh authorization first rejects every existing save,
then creates its origin receipt exclusively. No user-selected save path is accepted.

The existing full-runtime workflow step now runs a sequential orchestration script:
normal 75-test fresh invocation, successful exit and verified absence of its Java
PID, sealed hashes of the exact saved world and expected-state proof, then one
resume invocation of the SAME runFullRuntimeGameTestServer profile. Namespace
filtering selects one restart verification test; no cohort or placement repeats.
The second process must have a different PID; its permit is consumed once before
world loading. Modified files, missing origin, wrong run/commit, live producer,
player data, additional worlds and links are refused. The world hash excludes only
the session.lock file; no world data is deleted or migrated. Hashes and provenance
are fixture identification, not an authentication service or player-save support.

The producer records the actual 3377 expected states, 20 resource positions and two
shore positions after the already-qualified chunk reload. The second process loads
the same world path, checks all states, resources/negative-Y provenance, markers,
SavedData and duplicate refusal; freshness is checked at tick 1. Reported block
reads are 3377 + 20 + 2 = 3399, with the unchanged shared budget. Existing baseline
profile and production gate remain unchanged; external generation pack stays out
of the normal JAR. Automated same-profile process persistence does not prove client
rendering or player streaming. Exact-source CI must establish the outcome.

## New-world activation prerequisite: player-ticket loading — 2026-10-14

Written owner approval authorizes activation for newly created worlds and merging
PR63 only after essential qualification, with existing saves excluded. No release.
Before changing world selection or opening the automatic gate, the existing full
CI profile now observes the known natural 60% candidate through a NeoForge fake
server player's actual chunk tickets, moving 0.22 blocks/tick at view distance four.
The IOE fixture makes no direct neighborhood load or chunk-ticket request and
injects no freshness receipts. It calls the unchanged dispatcher every tick and
records the FULL neighbor count when the candidate is first admitted, its terminal
outcome, and duplicate refusal after all 25 neighbors eventually become available.

This is a controlled server-side player simulation, not visual client acceptance.
The observation can pass while activation_qualified is false: a recorded refusal
is evidence of an activation blocker, not a successful natural placement. The
workflow runs this one-test fresh fixture in the SAME existing full profile before
the existing 75-test/restart sequence. Only after its Java process has terminated
and its reported world path/source match does the script archive that execution's
world under build/tmp, outside the profile. It does not delete or open any unknown
save, add a profile, install a server, or transfer the CI restart permit.

Production remains closed until this prerequisite and new-world selection/mutation
guards are qualified. Existing terrain-selection gates, budgets and contracts are
unchanged. Exact-source CI must supply the observation and activation decision.

The vanilla login helper failed before admission because it did not negotiate Jade
custom payloads. The fixture now uses NeoForge FakePlayer and level.addNewPlayer
for normal entity/chunk tracking, with its standard no-op packet sink. It does not
exercise login, client packet handling or change production network validation.
The relevant proof is server player-ticket loading; it remains distinct from an
actual connected client journey. No mod is removed to make the fixture pass.

The first player-ticket run exposed accelerated GameTest ticks: thousands of ticks
elapsed before asynchronous terrain workers completed the final neighborhood. That
run is not a representative walking qualification. The observation now pauses
50 ms per fixture tick (hosted CI only), preserving real worker progress and the
unchanged 20-tick lease. No engine timing or production budget is modified.

The paced observation at source d6bd8c1231878f419232e89b718a5f5b33df6a52
(PR run 38502850852) reached the known candidate with 13/25 FULL neighbors and
returned TERRAIN_CHANGED; all 25 later loaded but replay was correctly refused.
A bounded scheduler correction now waits for all 25 FULL neighbors before FIRST
admission, using only existing lease entries and getChunkNow metadata queries.
It never renews the birth tick or queues new authority. At age 19 (last fresh tick),
it dispatches even if incomplete, preserving terminal refusal. Invalidated receipts
stay invalid. The paced fixture is rerun to determine whether this bounded wait is
sufficient; no success or production activation is inferred from the correction.

The corrected scheduler at source 84162e8100aef7961f8fcf362992bc90d4eb8217
passed push CI 38504162518 (701 JUnit; 75 baseline, one streaming observation,
75 full-runtime and one restart GameTest). The streaming admission waited until
elapsed tick 1438, but only 15/25 neighbors were FULL: TERRAIN_CHANGED remained
terminal, with all 25 subsequently available. Activation is still unqualified.

A separate fixture shutdown issue was observed in a superseded run: assertions
completed, then shutdown stalled at Saving worlds. Cleanup now removes the fake
player and waits through ordinary paced ticks until the Nether ChunkMap reports
no work for five ticks, bounded at 1000 ticks. It does not force unloads or mutate
internal maps. The existing consolidated CI job also has a 30-minute ceiling so
a shutdown stall cannot remain unbounded. This is a fixture correction, not a
claim to fix a production save defect. Its own exact-source CI remains required.

The first cleanup criterion was too broad: pinned ChunkMap.hasWork includes retained
loaded chunk holders as well as queued work. Both e34ce61 CI observations completed
but that assertion failed after 1000 ticks; both processes then stopped, so no
complete CI success is claimed for e34ce61. Cleanup now preserves the observed
1000 paced ticks of settling and requires an empty player list, zero pending main
chunk tasks and no light work for five consecutive ticks, with a 1200-tick ceiling.
It does not claim every retained chunk is unloaded. Actual process exit remains
mandatory before archiving or launching the regression world.


## Authorized bounded preparation — 2026-10-14

The owner explicitly authorized adjusting the tick bound as necessary after review
of the 20-tick incompatibility. Work remains modifications/tests on draft PR63 only;
merge and publication are forbidden. A preparation retains the original new-load
identity, never renews its birth tick and never persists across reload/restart.
All 25 FULL neighbors must be available before admission. Only that admission turn
can spend the retained identities; ordinary explicit transactions keep their 20-tick
lease. Unload, repeated/disk load, identity replacement, player break/entity place,
capacity denial and expiry still deny writing. Terminal admitted attempts never retry.

The initial qualification ceiling is 800 ticks (40 seconds at nominal tick rate).
Prior evidence had all neighbors by elapsed 1800 versus candidate load near 1419:
381 ticks, plus up to 64/0.22 = 291 ticks across the four-chunk preparation window
at this fixture's walking speed, totals 672; 800 provides a bounded margin without
claiming a universal loading guarantee. The revised SAME player-ticket fixture
records first complete neighborhood and candidate/oldest retained identity ages.
It now requires COMMITTED on unmodified natural terrain, not merely a recorded
refusal. This provisional ceiling must be reviewed against those exact-source
measurements before activation is called qualified. No new server/profile is added.


The new-world activation implementation is now prepared behind the CLOSED production
gate. Exact LevelStorageAccess repository binding establishes an immutable UUID and
schema/depth manifest only for directories without existing level.dat/level.dat_old
or region files. Unmarked existing saves remain excluded. WorldLoader.PackConfig
selects the world-scoped pack before WORLDGEN registry loading; temporary new-world
creation repositories use initMode and receive no mutation authority until bound
to an actual fresh save. Reopens require the original identity and selected pack.
Malformed identities fail closed, with no replacement or migration. All real write
adapters and natural admission check the bound world and depth. The external CI
prototype now exercises these SAME hooks instead of a global AddPackFinders source.
The runtime jar carries depth32 only inside a nested pack, not global vanilla data.
Hosted tests will check identity, pre-registry selection, bounds and restart identity;
client GUI flow and visual acceptance remain unproved. This is preparation/test work,
not production activation, merge or publication.


Bound measurement at source d3ac1474d1d0cf290e629ee5a5646c1f321f945c:
push CI 38537070875 and PR CI 38537076431 both completed successfully. The SAME
natural progressive route committed at elapsed 1660/1667, with 25/25 FULL neighbors,
candidate ages 241/248 and oldest retained window identities aged 387/394 ticks.
No IOE forced load, receipt injection, terrain edit or terminal replay was used.
The retained ceiling is 800 ticks: the next 100-tick bound above twice the observed
maximum 394 (788 rounded upward). This is a bounded variability margin for the
measured route, not a guarantee for all seeds, walking paths or hardware. Expired
preparation still loses its permission. These are d3ac147 measurements, NOT results
of the subsequent identity/registry/resource-verification changes. Their exact-source
CI remains required. The later 073839d test-source compile failure (counter assertion
before declaration) is corrected; no success is claimed for that superseded source.

World identity now also receives a check at ServerAboutToStart, before loadLevel:
selected pack, minY=-32, dimension height=288 and logical height=160 must agree.
Incompatible settings fail before terrain creation, and marked existing saves that
lost their selected pack cannot silently switch generation. Production remains closed.
Qualification now selects the nested runtime pack through the actual mod resource
path, identical to the prepared production path; the external prototype path remains
only the existing CI scope/provenance anchor. Client creation/recreation GUI flows
remain unqualified and must not be inferred from the server-side tests.


Admission grants are scoped to the immediate dispatcher call, separately from
candidate selection. On return they are revoked; the finished candidate and
consumed write identities cannot be reused. Unused neighboring preparations keep
their original birth dates and revocation state, so one refused candidate does not
silently discard a distinct neighboring region's preparation. Unit coverage checks
this cleanup boundary and rejects starting a selected candidate in a different tick.


## Crystal Cave — préparation de PR64, 2026-10-14

État distant re-vérifié : PR63 fusionnée, main `53cea49a2636939356c0bd2a8ce11c07a27f23af`.
La CI postmerge 38542822224 est verte ; ses 709 JUnit et 152 GameTests restent
propres à cette source, pas aux familles Crystal Cave futures. Routage de production Nether OFF ; sites Overworld activés par défaut.
Les anciennes mentions PR63 brouillon ci-dessus restent historiques.

La cible Crystal Cave inclut désormais explicitement améthyste et la création ou
l'intégration de Budding Fluix, Draconium et Glowstone, avec un bloc par phase et
la mécanique ExtendedAE. Voir [contrat de préparation](CRYSTAL_CAVE_CONTRACT.md)
et [inventaire cloud limité](CRYSTAL_CAVE_CLOUD_INVENTORY.json). L'usure fully→mostly→half→hardly
d'ExtendedAE n'est ni la croissance small→medium→large→cluster ni une qualité de site ;
fully s'use, contrairement au Flawless permanent actuel. Mapping, portée native,
produits et budgets nécessitent décision avant implémentation. Aucun comportement
modifié dans ce cycle documentaire, aucune famille fictivement déclarée terminée.
La CI du successeur documentaire ne transforme pas le JAR PR63 en implémentation
Crystal Cave ; reçu exact sur la PR. Validation client/visuelle toujours non prouvée.


## PR64 — quatre usurables et cinquième Flawless permanent

Décision confirmée, message vocal `01a13b6f-6a6e-70ba-bee1-95d780921357`.
Même branche/PR64 : premier cycle de code sur les 13 familles GeOre IOE existantes,
65 blocs/items, ancien registre conservé. FULLY descend vers FLAWED puis CHIPPED,
DAMAGED et stockage ; Flawless reste permanent. Paramètres ExtendedAE 1/3 et 1/10.
Budgets naturels/recettes/plafond de restauration FLAWED et blocs Certus natifs
conservés. Nouvelles familles et distributions Crystal Cave encore non implémentées.
Les tests adaptés et leur CI au head exact sont consignés dans le reçu PR64 ;
les résultats du commit documentaire 949e716 ne prouvent pas ce nouveau code.
Routage de production Nether OFF ; sites Overworld activés par défaut, aucun ancien monde modifié ; qualification visuelle non prouvée.


## PR64 — améthyste profil A

Reprise vérifiée sur le head local/distant `4e91a618df696de20be8584f5b45b922ee2a63b1`,
checkout propre, PR64 brouillon. La validation utilisateur « Pr64 IOE A »
(`Sentinel_4d0d913a968c8191831bc29dfcfe7479`, 14 octobre 2026 22:27 UTC) autorise
les cœurs récupérables IOE, produits/récoltes vanilla et résidu amethyst_block.

Cinq cœurs améthyste ajoutés par le moteur partagé : 14 familles/70 cœurs définis,
65 identités antérieures conservées. Native budding/cristaux et leurs loots non
remplacés ; aucune recette de cœur ou distribution naturelle ajoutée. Jade affiche
Minecraft et réutilise la provenance persistante existante. Tests de croissance,
récolte/registre, dépendances, exclusion du placement GeOre et Jade adaptés.
Le reçu PR64 distingue la nouvelle CI exacte de la qualification du head précédent.
Routage de production Nether OFF ; sites Overworld activés par défaut ; garde-fous Nether inchangés ; aucune installation, migration,
fusion ou publication. Validation visuelle/client toujours non prouvée.


## PR64 — réconciliation Glowstone et voisin absent

Reprise sur le head local/distant propre `e7f02eec00270c4d662e1c2e953ac521a97be959`.
La décision vocale Glowstone impose le moteur commun, pas le profil spécial
quatre poussières. Le contrat précise désormais la seule lacune de récolte des
cristaux ; ce choix ne bloque plus les travaux indépendants. AE2CS Fluix purifié
reste un candidat distinct sans dépendance ajoutée.

Correctif partagé : ne pas lire/générer un chunk voisin absent lors de randomTick,
ni tirer l'usure dans ce cas. GameTest sur l'entrée runtime réelle pour les familles
et rangs disponibles ; validation exacte à consigner dans PR64. Les 14 checks
verts de e7f02ee restent la preuve de la source précédente seulement.
Routage de production Nether OFF ; sites Overworld activés par défaut ; aucune famille/recette/distribution nouvelle ni modification des
budgets, read sets, déduplication ou anciens chunks. Visuel/client non prouvé.


## PR64 — famille Glowstone approuvée 2/1

Reprise sur le head local/distant propre cb473e39a35f46825ad8738c2a415c9e636b00c3.
Décision `01a14246-770a-729f-9495-94c30efb2f87` : deux poussières avec pioche,
une autrement, Fortune/Silk Touch communs conservés. Cinq cœurs et quatre produits
Glowstone implémentés avec le moteur partagé, stockage vanilla, Jade/persistance
existants. 15 familles / 75 cœurs ; aucune recette ou distribution naturelle ajoutée.
Tests runtime récolte/outils/Fortune à graines fixes, croissance/usure et Jade ;
CI exacte à consigner dans PR64, sans réutiliser les résultats cb473e3 comme preuve.
Routage de production Nether OFF ; sites Overworld activés par défaut ; contrats Nether, anciens chunks et protections inchangés.
Aucune installation/fusion/publication ; validation visuelle non prouvée.


## TesT play2 — qualification des versions actuelles

L’inventaire propriétaire de 08:02 UTC remplace les anciennes cibles Domum/Jade.
Cibles : AE2CS 1.2.1, Domum 1.0.236-snapshot, ExtendedAE 1.21-2.2.41-neoforge,
Jade 15.10.6, AE2 19.2.18 et GeOre 6.2.4. Les quatre SHA256 fournis sont
vérifiés sur les distributions officielles. Voir TEST_PLAY2_COMPATIBILITY.md.

Reprise vérifiée sur 9923ba62704ccec0308e3042e3763c806d1d4c07, branche existante
PR64. La qualification précédente a échoué sur un descripteur Jade incorrect dans
le vérificateur (IComponentProvider, pas IBlockComponentProvider) ; contrôle corrigé.
Aucune preuve de succès transférée aux nouveaux JAR.

Phase préalable : baseline complète restaurée (Domum 1.0.231/Jade 15.10.6), job
courant séparé avec admission temporaire exacte, compilation sur API antérieures,
contrôle ABI et GameTests complets/restart. Bornes distribuées encore inchangées.
Aucun JAR du job temporaire n’est distribué. Admission définitive après preuve CI.

Overworld actif par défaut, Nether fermé, OreGuard indépendant, retrogen désactivé.
Aucune modification gameplay, PC, mods tiers, MineColonies, installation, fusion ou
publication. Thunderbolt déjà installé sur PC, aucun ajout requis par ce patch.
Validation visuelle/client non prouvée.

### Admission après qualification préalable

Les quatre runs push/PR baseline/actuel du source 7929d53e580b77c95c3675a03e9abeea2ddb2edc
ont réussi (liens dans TEST_PLAY2_COMPATIBILITY.md). AE2WTLib 19.2.6, fixture déjà
connue, est aussi chargé dans le runtime minimal actuel pour AE2CS 1.2.1.
Les bornes distribuées ajoutent seulement AE2CS 1.2.1, Domum 1.0.236-snapshot et
ExtendedAE 2.2.41 ; Jade/AE2/GeOre inchangés. Overrides d’admission supprimés,
contrôles Maven et installateur ajoutés. Le commit final et son JAR sont soumis
à une nouvelle CI exacte ; reçu source/artefact distinct dans PR64. Aucun changement
métier, PC, installation, fusion ou publication. Visuel/client toujours non prouvé.


### BP-31 — complete-runtime fixture correction

At ea22bd7, the baseline PR complete runtime rejected the positive
`certusAndEntroFeatureTransactions` fixture for `entroized_fluix POOR`.
The catalogue matrix itself stayed within 2,497 cells. The special-profile
fixture and generic positive MINER_CAMP fixture now explicitly prepare and
check the same exact geometry metadata halo as the GeOre/catalogue fixtures.
The separate missing-neighbor test still requires rejection without forced
loads. Production guards, geometry, rewards and the 2,500-cell cap are unchanged.
This correction needs a new exact-source CI; ea22bd7 is not qualified.


### BP-26 — composed-budget correction

Candidate 9b18ada reached 2,920 cells in the composed MINER_CAMP Motherlode
matrix and C was refused by actual placement. The new cap trims unused rear
shoulders and tapers the intermediate viewing space around the exposed single
stem. Walkable base/underside routes, upper cap, B's slit and C's isthmus remain;
no main passage is narrowed. The 2,500-cell guard, fixed masks and rewards are
unchanged. New exact-source CI is required; the failed candidate is not qualified.


BP-26 follow-up: ca30d42 reduced the matrix maximum to 2,573 cells; only the
composed-cap test still failed, on C/Motherlode camp. C now excavates the upper
faces of its two lobes and the actual separating views beside the isthmus,
instead of the full cap's unused upper shoulders. Supported routes, underside,
fixed masks and the production cap remain unchanged. Exact CI required again.


BP-26 follow-up: a9b7c6c reached 2,549 cells, now on B/Motherlode camp;
the cap remained the only failing test. The underside now follows the supported
routes and exposed stem faces instead of unused cap corners; an unused lower
pocket north of the peripheral descent is removed. Width/return/stem tests are
unchanged, as are the upper cap, B's slit, C's isthmus, rewards and 2,500-cell guard.
New exact-source CI is required.


### BP-36 — supported N2 approach

Review of the first candidate found that the rear descent's backing occupied
one cell of the N2 approach turn. B now places its rear ladder on the western
rim (X7, backing X6), with the landing at X8. The N2 branch uses X8/9 and its
full two-wide turn stays open. Explicit support/clearance assertions cover the
branch and landing. The rear L-shaped view, fixed masks and budgets remain;
qualification must use the corrected source, not e4ab137.


### BP-36 — composed budget failure and correction candidate

f640448 reached 2,584 composed cells for MINER_CAMP/MOTHERLODE, variant B.
The baseline minimal runtime completed 113 tests with only the composed-cap
test failing. The correction removes an unused lower north-east shoulder and
intermediate viewing corners, and retains a two-wide western lower route.
The shared volumes, three openings, equipped descents, N2 turn and upper rib
remain covered by the existing route/cut tests. The 2,500-cell cap is unchanged.
This candidate requires new exact-source CI; 42/120 remain qualified and
45/120 implemented until that qualification succeeds. No client proof or install.


BP-36 follow-up: 35c06ff reached 2,544 cells, with only the composed-cap
test failing among 113 current minimal GameTests. The unused upper south-east
slab shoulder is now omitted; the north/east approach, western bypass, rear
view, and C's two-wide rib remain. No budget, cap or route assertion is relaxed.
A new exact-source qualification is required.


BP-36 follow-up: 08a697f reached 2,514 cells; the composed cap was again
the only failure among 113 baseline minimal GameTests. The remaining unused
lower north corner and intermediate east viewing strip are removed. The
central open column and two-wide lower connections remain. All existing
behavioral assertions and the 2,500-cell guard are retained; exact CI required.


### BP-17 — distinguish a free tip from host wall in the signature

4ff0ae9 failed three tests at the shared second-tip signature. X6/Y6/Z6 is
host wall outside A/C's narrower cavity, so STONE there is not evidence of
a second suspended tip. The signature now requires B's second tip and its
free underside; the separate between-tip window and roof-continuity tests
remain. No geometry or budget assertion changes. The matrix aborted before
its maximum receipt, so no cap result is claimed for this candidate.
New exact-source CI is required.


### BP-17 — composed-budget correction

643c588 completed the signatures/routes/windows but reached 2,605 composed
cells for the C Motherlode camp; only the cap test failed among 117 current
minimal GameTests. Viewing space now follows the actual taper at each height,
with faceted corners. Unused lower rear shoulders, upper north-west space and
one upper rear edge are omitted. The free-tip crossing, exposed flanks, roof
attachments, two-wide routes and independent window cut tests remain; flank
probes follow the real narrowing tip. The 2,500-cell cap is unchanged.
New exact-source CI is required, with no prior success transferred.


### BP-08 cycle — BP-03 satellite composed-budget correction

e1e718e failed only the composed-cap test in the current minimal runtime:
2,502 cells, MINER_CAMP/MOTHERLODE, depth/variant710 (depth28, PEARLS_SATELLITE),
anchor(-21,90,57). The 87-way shape matrix exposes a different camp fixture
for the older BP-03 C form. Its extra middle-roof height now tapers to a smaller
rounded crown; all three walking/headroom cells, footprint, necks and satellite
remain. An assertion checks three-high clearance and the retained raised centre.
No cap, mineral mask, budget or route assertion is relaxed. This correction needs
fresh exact-source CI; 84 variants remain qualified and 87 implemented.
