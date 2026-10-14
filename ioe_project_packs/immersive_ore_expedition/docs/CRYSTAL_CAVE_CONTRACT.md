# Crystal Cave — contrat de préparation et mécanique vérifiée

Statut : premier cycle du modèle à cinq rangs implémenté pour les familles GeOre IOE existantes ; cave et nouvelles familles non générées. Ce document poursuit le chantier Crystal Cave existant et ne remplace
pas les contrats Nether, IE, Budding ou météorites par des hypothèses implicites.
La validation visuelle/client demeure non prouvée.

## Décisions confirmées

- Crystal Caves de l'End ; poches organiques irrégulières d'environ 24–32 blocs
  d'un bord à l'autre selon les axes, sans sphère parfaite ni axes égaux imposés.
- Mélange de quartz, Certus, Fluix, Entro, Draconium, diamant, redstone, lapis,
  émeraude, Glowstone et **améthyste**. La palette mixte remplace la séparation
  des familles, sans imposer de pondérations égales.
- Quartz fumé comme contraste sombre entre budding et end stone ; pas de blackstone.
- **Intégrer ou créer les Budding Fluix, Draconium et Glowstone est autorisé.**
  L'absence d'un fournisseur natif ne remet plus en question cette création.
- Cible demandée : mécanique ExtendedAE appliquée à tous nos buddings, avec un
  bloc distinct pour chaque phase de budding. La correspondance aux rangs IOE
  est définie ci-dessous : croissance des cristaux, usure du cœur et
  qualité du site sont trois notions différentes.
- Les autres cristaux repérés restent des candidats, pas des ajouts autorisés.

Provenance : fil vocal `01a11e32-a44d-7245-9778-b3d9dc4a90c4`, extraits retrouvés
et vérifiés par le fil délégant. Géométrie : messages
`01a136cb-6560-7179-9708-0526221a5d57` et
`01a136cc-18a0-7531-b335-8e9c547230eb`. Quartz fumé :
`01a136d1-e4da-704c-a96f-a629587cfa03`. End, Draconic Evolution et Fluix Budding :
`01a136d8-f593-72ea-920d-16d28fc411e3`. Améthyste :
`01a13b28-8ab8-71de-ab5d-0a4d0517bf3a`. Création des trois familles et mécanique :
`01a13b29-47cb-775f-861b-48670fbfdc1c`, `01a13b29-72d4-744a-82a7-d52250526802`,
`01a13b29-dc9f-7332-bad1-1cf52079f926`, `01a13b2d-2834-716f-bb4e-4157bbb95670`,
`01a13b2d-80d2-717c-bb25-0daf9214abc7`, `01a13b2f-55db-74fb-84c2-724eda532bae`.
Ces extraits ont été transmis à l'exécutant ; aucun accès direct au fil vocal ou
au PC/Prism complet n'est revendiqué. « PR764 » est rectifié en PR64 dans le mandat.

## ExtendedAE : faits de source, pas traduction arbitraire de qualité

Référence : ExtendedAE **1.21-2.2.33-neoforge**, révision
`90005ee29839fb9fa83bbe6544919c722f8b0dc6`, correspondant à la version épinglée.
JAR inspecté : SHA256
`6652ed1ea4b71f585d48c05a195a77594a7a2bd1ecea0fc805db2122aafad734`.

Sources immuables :
- [BlockBuddingEntro.java](https://github.com/GlodBlock/ExtendedAE/blob/90005ee29839fb9fa83bbe6544919c722f8b0dc6/src/main/java/com/glodblock/github/extendedae/common/blocks/BlockBuddingEntro.java)
- [BlockEntroCluster.java](https://github.com/GlodBlock/ExtendedAE/blob/90005ee29839fb9fa83bbe6544919c722f8b0dc6/src/main/java/com/glodblock/github/extendedae/common/blocks/BlockEntroCluster.java)
- [EAESingletons.java — déclarations et registres](https://github.com/GlodBlock/ExtendedAE/blob/90005ee29839fb9fa83bbe6544919c722f8b0dc6/src/main/java/com/glodblock/github/extendedae/common/EAESingletons.java)
- [Licence LGPL version 3](https://github.com/GlodBlock/ExtendedAE/blob/90005ee29839fb9fa83bbe6544919c722f8b0dc6/LICENSE.txt)

### Croissance du cristal adjacent

À chaque random tick du cœur, une tentative a lieu avec probabilité **1/3**.
Une des six directions est choisie. L'air ou l'eau source (quantité 8) permet
un petit cristal. Sinon un cristal Entro orienté dans la même direction avance
exactement d'un stade :

`entro_cluster_small → entro_cluster_medium → entro_cluster_large → entro_cluster`

Ce sont quatre **blocs de croissance**, tous sous `extendedae:`. Le cluster final
n'avance plus. Un voisin incompatible arrête la tentative. Orientation et eau sont
conservées dans l'état du nouveau cristal. Ce n'est pas une probabilité de 1/3 par
seconde : les random ticks et la disponibilité de la face conditionnent le résultat.

### Usure du cœur producteur

Seulement après une croissance admissible et l'appel de placement du cristal,
le code tire une probabilité **1/10** d'usure. Il ne vérifie pas le booléen renvoyé
par `setBlockAndUpdate` avant ce tirage ; reproduire exactement cette subtilité
ou imposer une croissance effectivement écrite devra être conscient et testé.

`extendedae:entro_budding_fully → extendedae:entro_budding_mostly →`
`extendedae:entro_budding_half → extendedae:entro_budding_hardly → ae2:quartz_block`

Les quatre cœurs instancient la même classe. **Fully se dégrade aussi** ; aucune
phase permanente n'est définie ici. Ces phases ne sont ni DRY/POOR/NORMAL/RICH/
MOTHERLODE ni les quatre tailles du cristal. La destination finale est le **bloc
Certus AE2**, pas Fluix ordinaire, Entro, ni air.

Les cœurs et cristaux sont détruits par piston. Les cristaux ont FACING et
WATERLOGGED, demandent un support solide et disparaissent si ce support disparaît.
Leur lumière vaut respectivement 5/6/7/8. Le code ne fournit pas à lui seul une
règle universelle de restauration ou un budget de site.

Les loot tables du JAR distinguent également les formes : les quatre cœurs donnent
`extendedae:entro_dust` ; les petits/moyens/grands cristaux donnent leur propre bloc
avec Silk Touch, sinon `extendedae:entro_shard`. Le cluster mûr possède sa table
séparée. Ces produits Entro ne doivent pas être copiés comme récompenses Fluix,
Draconium, Glowstone ou améthyste. Aucun taux de loot IOE n'est déduit de cet inventaire.

### Différence avec l'implémentation IOE actuelle

Avant ce cycle, `GeOreBuddingBlock` utilisait une tentative 1/5 et une usure 1/12 ; les rangs étaient
DAMAGED/CHIPPED/FLAWED/FLAWLESS. FLAWLESS est permanent et réservé aux Motherlodes.
Les cœurs épuisés deviennent le stockage propre à leur famille. Certus utilise
les blocs natifs AE2. Remplacer quelques constantes ne suffit donc pas à appliquer
ExtendedAE à tous les buddings : mapping des phases, identité des blocs, restauration,
Jade, sauvegardes et traitement des blocs natifs doivent être explicités.

**Décision résolue : quatre rangs usurables puis un cinquième Flawless permanent.**
Confirmation utilisateur « c'est ça », message `01a13b6f-6a6e-70ba-bee1-95d780921357`
du même fil vocal. FULLY reste distinct de FLAWLESS. Dans IOE :
`FULLY → FLAWED → CHIPPED → DAMAGED → stockage de famille`, avec FLAWLESS hors usure.
Les anciens identifiants et ordinals sont conservés ; FULLY est ajouté, sans migration. Les règles Nether et Entro réservé à IE sont conservées.
Réutiliser une mécanique ExtendedAE pour d'autres minéraux n'autorise pas une
famille IOE Entro ni la génération naturelle de ses cristaux.

## Blocs disponibles et créations nécessaires

| Ressource approuvée | Disponible et forme réelle | Travail requis |
| --- | --- | --- |
| Améthyste | `minecraft:budding_amethyst`, `small_amethyst_bud`, `medium_amethyst_bud`, `large_amethyst_bud`, `amethyst_cluster`, `amethyst_block` (tous minecraft) ; item `minecraft:amethyst_shard` | Le vanilla fournit un seul cœur, pas quatre phases d'usure. Adapter/créer les cinq phases IOE confirmées, sans changer arbitrairement les géodes vanilla. |
| Quartz | `geore:budding_quartz`, `geore:small_quartz_bud`, `medium_quartz_bud`, `large_quartz_bud`, `quartz_cluster` (préfixe geore) | Produits disponibles, adaptation des phases requise ; distinct du Certus. |
| Certus | `ae2:damaged_budding_quartz`, `chipped_budding_quartz`, `flawed_budding_quartz`, `flawless_budding_quartz` ; quatre produits natifs | Quatre rangs déjà disponibles, mais mécanique AE2 distincte. Préserver météorites/progression AE2 tant qu'aucun traitement natif précis n'est décidé. |
| Diamant/redstone/lapis/émeraude | Familles IOE existantes à cinq rangs ; `geore:small_<famille>_bud`, `medium_<famille>_bud`, `large_<famille>_bud`, `<famille>_cluster` | Réutiliser les familles existantes ; ne pas doubler les registres. |
| Fluix | `ae2:fluix_block` est un bloc ; `ae2:fluix_crystal` est un item | Cœurs à phases et cristaux de croissance Fluix à intégrer/créer : aucun fournisseur identifié dans les JAR inspectés. **Pas Entro.** |
| Draconium | Source Draconic Evolution : `draconicevolution:end_draconium_ore`, `draconium_block` ; item `draconium_dust` (même namespace) | Fournisseur confirmé, JAR absent de l'inventaire cloud. Cœurs/cristaux à intégrer/créer ; version dépendance et produit final à fixer. Awakened non ajouté. |
| Glowstone | `minecraft:glowstone` bloc et `minecraft:glowstone_dust` item | Cœurs à phases et produits cristallins à intégrer/créer. La création est autorisée ; la quantité de poussière par récolte ne l'est pas encore. |
| Entro | Quatre cœurs et quatre cristaux ExtendedAE ci-dessus | Disponibilité ne vaut pas autorisation de génération IOE. Extraction IE conservée. |
| Quartz fumé | Candidat source Botania `botania:smokey_quartz_block` | Rôle décoratif acquis ; distribution 1.21.1 NeoForge encore à qualifier, aucun JAR cloud présent. |

Draconic Evolution, source vérifiée :
[DEContent.java](https://github.com/Draconic-Inc/Draconic-Evolution/blob/6251e32bc93eb4bfcd910236ec1eaa6546516229/src/main/java/com/brandon3055/draconicevolution/init/DEContent.java).
Botania : [source 1.21.1-porting](https://github.com/VazkiiMods/Botania/tree/f0367399c2c3897b9384da886e155b479e60b329).
Aucun nouveau namespace/ID IOE n'est annoncé comme enregistré dans ce cycle.

## Inventaire cloud et candidats supplémentaires

[Inventaire reproductible des fichiers présents](CRYSTAL_CAVE_CLOUD_INVENTORY.json) :
306 JAR repérés sous /workspace, caches cachés compris ; cinq dépendances mod uniques
inspectables après exclusion des anciens artefacts IOE et outils de build :
AE2 19.2.17, AE2CS 1.1.12, GeOre 6.2.2, ExtendedAE 2.2.33 et GeOre Additions 4.0.
Ces cinq fichiers sont des téléchargements d'inspection, pas la preuve de mods
installés ensemble. Le manifeste CI peut nommer d'autres mods sans que leurs JAR
soient présents ici. **Ce n'est pas l'inventaire complet Prism/modpack du PC.**

Vanilla : améthyste dispose effectivement des quatre formes de croissance.
Quartz, diamant et émeraude vanilla sont des ressources/items/minerais, pas des
familles budding vanilla. `minecraft:prismarine_crystals` est un item ;
`minecraft:end_crystal` est un item plaçant une entité, pas un bloc de cluster.
Ces deux derniers restent candidats d'inventaire, sans ajout à la cave.

AE2CS expose notamment les blockstates `ae2cs:rose_quartz_block`,
`ae2cs:ender_quartz_block`, `ae2cs:redstone_crystal_block`,
`ae2cs:meteor_crystal_block`, `ae2cs:resonating_crystal_block`,
`ae2cs:irradiated_crystal_block` et `ae2cs:quantum_crystal_block`.
Ce sont des candidats de blocs, pas la preuve de budding correspondant.
GeOre contient d'autres familles telles que ruby, sapphire, topaz, monazite,
osmium et platinum ; présence d'assets ne prouve ni activation runtime ni accord
pour les ajouter. Black quartz n'est pas une substitution au quartz fumé.
GeOre Additions 4.0 ne contient que les deux blockstates d'extracteurs et n'apporte
aucune des trois familles manquantes.

## Licence et limites de réutilisation

ExtendedAE est LGPL3 selon la licence à la révision inspectée ;
[THIRD_PARTY_NOTICES](../THIRD_PARTY_NOTICES.md) conserve la provenance.
Ce cycle décrit le comportement et référence les sources, sans copier Java, modèles,
textures ou tables de loot dans IOE. Il n'étend pas cette licence aux assets des
autres mods : AE2, AE2CS, GeOre, Botania et Draconic Evolution conservent leurs
propres conditions. Pas d'extraction d'assets tiers pour fabriquer les nouvelles familles.

## Décisions encore indispensables à l'implémentation

1. **Restauration future** : le modèle quatre usurables + Flawless permanent est
   décidé et implémenté pour les 13 familles existantes. Le plafond de restauration
   FLAWED et les recettes existantes restent inchangés ; aucune recette FULLY ajoutée.
2. **Portée native** : « tous nos buddings » inclut-il la modification des blocs
   natifs AE2/GeOre/vanilla hors IOE, ou les variantes IOE seulement ? Préserver par
   défaut leurs registres et les météorites ; ne pas migrer les anciens mondes.
3. **Produits et épuisement** : produits finaux exacts et quantités de récolte,
   Silk Touch/Fortune, stockage final des nouvelles familles et de l'améthyste.
   Le quartz final d'ExtendedAE ne doit pas devenir automatiquement le résidu universel.
4. **Budgets cave** : total et répartition par famille/qualité, phases initiales,
   clusters préexistants, densité Glowstone, plage Y, biomes, fréquence et espacement.
   End, forme approximative 24–32 et palette sont déjà décidés.
5. **Dépendances** : qualifier les distributions Draconic Evolution et quartz fumé
   avant référencement runtime. Aucun téléchargement/installation de mod supplémentaire
   n'est nécessaire pour trancher les questions de gameplay ci-dessus.

Proposition technique, non appliquée : séparer dans l'implémentation la taille du
cristal, la phase d'usure du cœur et la qualité du site ; employer des noms Jade
sans ambiguïté et réutiliser la persistance existante. Conserver les anciens identifiants des blocs sauvegardés et ne pas migrer les chunks. Ne créer aucun test isolé d'un modèle
inventé pour donner l'impression que cette décision est déjà prise.

## Premier cycle d'implémentation du modèle à cinq rangs

Les 13 familles GeOre IOE enregistrent désormais cinq blocs/items distincts (65) :
13 `fully_budding_<famille>` ajoutés aux 52 identités conservées. FULLY affiche un
modèle avec moins de fractures que FLAWED, distinct du Flawless intact, et une
traduction EN/FR. Jade lit son rang réel sans le confondre avec la qualité du site.
La persistance de provenance reste inchangée : elle ne stocke pas de rang initial.

La croissance des cœurs IOE suit les paramètres vérifiés ExtendedAE : tentative
1/3, usure 1/10 après croissance, quatre étapes d'usure ; Flawless permanent est
l'exception explicitement approuvée. Les produits GeOre et stockages finaux restent
ceux du contrat de chaque famille. FULLY suit la règle de récolte déjà établie des
rangs usurables : lui-même avec Silk Touch, rang inférieur FLAWED sinon. Aucun
rendement de cristal, nouvelle recette ou récompense des nouvelles familles inventé.
Flawless conserve sa récolte historique en FLAWED, jamais en item permanent.

Les budgets/placements existants n'ajoutent pas FULLY naturellement. Aucune fréquence
Crystal Cave, aucun cœur Fluix/Draconium/Glowstone/améthyste enregistré dans ce cycle.
Les quatre rangs natifs Certus sont énumérés séparément ; aucune tentative de trouver
un faux `ae2:fully_budding_quartz`. Les observations ExtendedAE associent fully à
FULLY et ne déclarent plus un cœur natif permanent qui n'existe pas.

GameTests adaptés : croissance, faces, eau, blocage, usure et permanence sur les
blocs réels, comparaison aux quatre phases ExtendedAE lorsque ce mod est chargé,
65 identités/items, récolte/restauration existantes, Jade avec rechargement des
métadonnées. Tests unitaires de compatibilité des quatre anciennes identités,
chaîne complète et séparation des phases natives. Validation des 65 modèles et
ressources. Résultats exacts consignés dans la PR, sans qualification visuelle.
Production OFF, aucun profil/serveur nouveau, ancien monde modifié ou chargement forcé.
