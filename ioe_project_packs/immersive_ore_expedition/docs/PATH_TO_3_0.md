# Parcours vers 3.0 — consolidation approuvée, aucune publication autorisée

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
