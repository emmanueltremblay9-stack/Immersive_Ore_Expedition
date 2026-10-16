# Parcours vers 3.0 — consolidation approuvée, aucune publication autorisée

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
propres à cette source, pas aux familles Crystal Cave futures. Production OFF.
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
Production OFF, aucun ancien monde modifié ; qualification visuelle non prouvée.


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
Production OFF ; garde-fous Nether inchangés ; aucune installation, migration,
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
Production OFF ; aucune famille/recette/distribution nouvelle ni modification des
budgets, read sets, déduplication ou anciens chunks. Visuel/client non prouvé.


## PR64 — famille Glowstone approuvée 2/1

Reprise sur le head local/distant propre cb473e39a35f46825ad8738c2a415c9e636b00c3.
Décision `01a14246-770a-729f-9495-94c30efb2f87` : deux poussières avec pioche,
une autrement, Fortune/Silk Touch communs conservés. Cinq cœurs et quatre produits
Glowstone implémentés avec le moteur partagé, stockage vanilla, Jade/persistance
existants. 15 familles / 75 cœurs ; aucune recette ou distribution naturelle ajoutée.
Tests runtime récolte/outils/Fortune à graines fixes, croissance/usure et Jade ;
CI exacte à consigner dans PR64, sans réutiliser les résultats cb473e3 comme preuve.
Production OFF ; contrats Nether, anciens chunks et protections inchangés.
Aucune installation/fusion/publication ; validation visuelle non prouvée.
