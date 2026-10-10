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
