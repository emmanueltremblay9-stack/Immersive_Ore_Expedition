# TesT play2 : admission et qualification

Inventaire propriétaire 08:02 UTC ; remplace Domum 1.0.234/Jade 15.10.5.
État préalable à la qualification : aucune borne distribuée élargie.

## Toutes les contraintes de chargement IOE

| Composant | Type / côté | Borne distribuée | Inventaire PC communiqué / admission |
|---|---|---|---|
| javafml | loader | `[4,)` | À confirmer avec le journal du pack |
| neoforge | required / BOTH | `[21.1,)` | Version non communiquée dans cet inventaire |
| minecraft | required / BOTH | `[1.21.1,1.22)` | 1.21.1 — admis |
| journeymap | optional / CLIENT | `[1.21-6.0.0-beta.1,)` | Version non communiquée dans cet inventaire |
| ae2 | required / BOTH | `[19.2.17,20)` | 19.2.18 — admis |
| ae2cs | required / BOTH | `[1.1.12,1.2)` | 1.2.1 — refusé |
| geore | required / BOTH | `[6.2.2,7)` | 6.2.4 — admis |
| geore_additions | optional / BOTH | `[0,)` | Version non communiquée dans cet inventaire |
| extendedae | optional / BOTH | `[1.21-2.2.33-neoforge]` | 1.21-2.2.41-neoforge — refusé |
| immersiveengineering | optional / BOTH | `[12.4.2-194,12.5)` | Version non communiquée dans cet inventaire |
| immersivepetroleum | optional / BOTH | `[4.4.1-37],[4.5.0-39]` | Version non communiquée dans cet inventaire |
| domum_ornamentum | optional / BOTH | `[1.0.231,1.0.232)` | 1.0.236-snapshot — refusé |
| jade | optional / BOTH | `[15.10.6,16)` | 15.10.6 — admis |

Les dépendances optionnelles imposent leur borne lorsqu’elles sont installées.
Les versions de compilation sont distinctes : Java 21, Minecraft 1.21.1,
NeoForge 21.1.230, JourneyMap API 2.0.0-1.21.1, Domum API 1.0.231,
Jade 15.10.6 et IE 1.21.1-12.4.2-194. Les coordonnées baseline AE2 19.2.17,
AE2CS 1.1.12 et GeOre 6.2.2 restent inchangées dans gradle.properties.
Les autres JAR CI (biomes, bibliothèques, IP, etc.) sont entièrement inventoriés
avec URL et SHA512 dans ci/runtime-mods.json ; ce sont des fixtures de test,
pas des contraintes de chargement ajoutées au pack utilisateur.

## Identités officielles des quatre cibles

- [ae2cs-1.21.1-1.2.1.jar](https://cdn.modrinth.com/data/uJ9afomy/versions/taIhgdOA/ae2cs-1.21.1-1.2.1.jar) : SHA256 `fb0d921c68d43e3ff76c9da16b6e3b8ba4944849064fd42aeb27e0f9ed099dde`.
- [domum-ornamentum-1.0.236-snapshot-main.jar](https://ldtteam.jfrog.io/ldtteam/modding/com/ldtteam/domum-ornamentum/1.0.236-snapshot/domum-ornamentum-1.0.236-snapshot-main.jar) : SHA256 `fcf875b183f9096b868b527837dfb677e397a11510cec79bdb01c7268b918f27`.
- [ExtendedAE-1.21-2.2.41-neoforge.jar](https://github.com/GlodBlock/ExtendedAE/releases/download/1.21-2.2.41-neoforge/ExtendedAE-1.21-2.2.41-neoforge.jar) : SHA256 `7484b0a2372e3b639f534e14acc274a15c6c94ba51063752f00aaa3dd23e6f12`.
- [Jade-1.21.1-NeoForge-15.10.6.jar](https://cdn.modrinth.com/data/nvQzSEkH/versions/eYz2YBGT/Jade-1.21.1-NeoForge-15.10.6.jar) : SHA256 `276f1f65a20d04f536989c4f311949d770b52c440c23c2b904a08860f3c93810`.

## Matrice API et comportement

| Intégration | Baseline → actuelle | Contrôle |
|---|---|---|
| Domum | 1.0.231 → 1.0.236-snapshot | Descripteurs JVM publics du constructeur Map, getTexturedComponents, readFromItemStack, updateTextureDataWith/getTextureData ; placement quatre rotations, matériaux, NBT, drops et rollback |
| Jade | 15.10.6 → identique | ABI d’enregistrement, découverte réelle du plugin, données/tooltip/provenance ; absence bloquante en runtime complet |
| AE2CS | 1.1.12 → 1.2.1 | Pas de liaison Java directe IOE : registres/recettes ; chargement des recettes Aggregator IOE, exclusion Flawless et récompense resonating_seed |
| ExtendedAE | 2.2.33 → 2.2.41 | Pas de liaison Java directe IOE : blocs des registres ; comparaison native des tirages, croissance, usure, orientation et waterlogging |
| AE2 / GeOre | 19.2.17 / 6.2.2 → 19.2.18 / 6.2.4 | Chargement et registres, croissance, récolte, tags, persistance et GameTests communs |

Phase 1 : CI ordinaire baseline et CI TesT play2 actuelle, mêmes sources, admission
exacte temporaire dans le job courant seulement ; aucun artefact distribuable de ce
job. Phase 2 seulement après succès : unions étroites des versions qualifiées,
tests d’admission et nouvelle CI exacte, JAR avec classes/métadonnées et SHA256.
Résultats encore en attente ; aucune compatibilité runtime annoncée sur inspection.

## Licence et périmètre

Aucun code ni asset tiers copié dans IOE et aucun JAR tiers embarqué/redistribué.
Les distributions officielles servent uniquement aux fixtures CI. Métadonnées
inspectées : AE2CS et ExtendedAE LGPL-3.0 (AE2CS LGPL-3.0-or-later).
domum déclare `GPL3` dans son JAR vérifié.
jade déclare `CC-BY-NC-SA-4.0` dans son JAR vérifié.

Thunderbolt 2.0.3-beta2 est déjà installé côté PC ; aucun ajout dans ce patch.
Deux MineColonies restent un problème du pack distinct. Aucun accès PC, monde lancé,
nettoyage, modification de sécurité ou installation. Overworld TRUE, Nether OFF,
OreGuard indépendant et retrogen OFF conservés. Validation visuelle/client non prouvée.

## Premier résultat de qualification actuelle

Le job 38872340400 au commit 9e7704f compile IOE puis échoue au démarrage
JUnit : AE2CS 1.2.1 référence de/mari_023/ae2wtlib/api/gui/Icon. Les tests ne
sont pas comptés comme exécutés. AE2WTLib 19.2.6, déjà épinglé dans le runtime
complet baseline, est désormais résolu aussi dans le runtime minimal actuel.
L’entrée stagée correspondante est retirée du seul manifeste actuel pour éviter
un doublon. Aucune dépendance inconnue ni modification du PC ; qualification à
reprendre avant toute admission distribuée.
