# IOE — Décisions finales sur les Budding

> **Statut : `FINAL_DESIGN`.** Ce document définit la cible canonique de gameplay. Il ne constitue pas une preuve que le code, les recettes, les assets, les tests ou le runtime sont déjà alignés. La validation d'implémentation doit passer par GitHub Actions et une preuve runtime distincte.

Ce document remplace toutes les anciennes règles de poids, de budgets, de rangs et de récompenses Budding. Les anciennes valeurs ne doivent plus être présentées comme la cible de gameplay.

## 1. Distribution des qualités de site

Le tirage naturel utilise les poids suivants :

| Qualité du site | Poids | Probabilité brute |
| --- | ---: | ---: |
| `DRY` | 10 | 10 % |
| `POOR` | 25 | 25 % |
| `NORMAL` | 45 | 45 % |
| `RICH` | 17 | 17 % |
| `MOTHERLODE` | 3 | 3 % |
| **Total** | **100** | **100 %** |

Ces probabilités sont celles du tirage de qualité après qu'un site a atteint cette étape. Elles ne représentent pas une fréquence finale par chunk.

## 2. Matrice finale des sites

Le minerai indiqué exclut le cœur Budding central de chaque node.

| Qualité du site | Nodes | Minerai par node | Minerai total | Rang initial |
| --- | ---: | ---: | ---: | --- |
| `DRY` | 0 | 0–5 dans la poche | 0–5 | Aucun |
| `POOR` | 3 | 4 | 12 | `DAMAGED` |
| `NORMAL` | 4 | 5 | 20 | `CHIPPED` |
| `RICH` | 5 | 6 | 30 | `FLAWED` |
| `MOTHERLODE` | 7 | 7 | 49 | `FLAWED`, avec 7,77 % pour un seul `FLAWLESS` |

## 3. Exclusivité absolue du Flawless

Tous les Budding `FLAWLESS`, y compris les Budding Certus natifs d'AE2, sont exclusifs aux sites `MOTHERLODE`.

- Aucune recette, machine ou amélioration ne peut créer un Budding Flawless.
- Aucun Flawless n'est permis dans `DRY`, `POOR`, `NORMAL` ou `RICH`.
- Un site ne peut jamais contenir plus d'un Flawless.
- Le tirage est effectué une seule fois par site, jamais une fois par node.

Cette règle couvre :

- `ae2:flawless_budding_quartz` ;
- les variantes GeOre Flawless fournies par IOE ;
- les futures intégrations utilisant le rang universel `FLAWLESS`.

La recette Flawless actuellement fournie par AE2 Crystal Science doit être supprimée ou neutralisée par le datapack IOE :

```text
6 × ae2cs:certus_quartz_seed
6 × #ae2cs:pure_crystal/resonating_crystal
6 × ae2:flawed_budding_quartz
→ ae2:flawless_budding_quartz
```

Après application des règles IOE, cette recette ne doit plus apparaître dans l'Aggregator, JEI ou EMI.

### Tirage Motherlode

Chaque site `MOTHERLODE` contient sept nodes. Par défaut, il contient `7 × FLAWED`.

Un seul tirage à `7,77 %` est effectué pour le site :

- réussite : `1 × FLAWLESS` et `6 × FLAWED` ;
- échec : `0 × FLAWLESS` et `7 × FLAWED`.

Le Flawless remplace un Flawed ; il ne crée pas un huitième node. Un fallback de `MOTHERLODE` vers une qualité inférieure doit supprimer toute sélection Flawless.

## 4. Mécanique AE2 reproduite par les variantes GeOre

Les variantes GeOre fournies par IOE reproduisent la mécanique réelle des Budding de la version AE2 chargée. IOE ne doit pas en créer une approximation fondée sur une ancienne documentation.

### Chaîne de dégradation

```text
FLAWLESS
    └─ ne se dégrade pas

FLAWED
    ↓
CHIPPED
    ↓
DAMAGED
    ↓
bloc minéral de stockage correspondant
```

Lorsqu'un Budding non Flawless déclenche la condition native de dégradation pendant la croissance, il descend exactement d'un rang. Le Budding `DAMAGED` devient le bloc minéral de stockage de sa famille lorsqu'il est épuisé.

Exemple Iron :

```text
immersive_ore_expedition:flawed_budding_iron
↓
immersive_ore_expedition:chipped_budding_iron
↓
immersive_ore_expedition:damaged_budding_iron
↓
minecraft:iron_block
```

L'implémentation doit reproduire les paramètres de la version AE2 réellement chargée :

- déclencheur et condition de croissance ;
- chance ou condition de dégradation ;
- ordre de dégradation ;
- faces pouvant produire une pousse ;
- contraintes de placement et logique de mise à jour ;
- comportement du Flawless ;
- restauration des rangs non Flawless ;
- interaction avec les accélérateurs compatibles.

Les rangs `DAMAGED`, `CHIPPED` et `FLAWED` peuvent être restaurés selon la mécanique AE2 correspondante. La restauration ne peut jamais dépasser `FLAWED`.

## 5. Récompense DRY

Un site `DRY` contient :

```text
0 node actif
0 à 5 blocs de minerai résiduel
10 % de chance de contenir 1 ae2cs:resonating_seed
```

La Resonating Seed reste neutre et ne contient aucune identité minérale. Avec un poids `DRY` de 10 %, la probabilité brute combinée est `10 % × 10 % = 1 %` avant les autres rejets du worldgen.

Dans l'Aggregator :

```text
Resonating Seed
+ Purified Resonating Crystal
+ bloc de stockage minéral
→ Budding Damaged de la famille correspondante
```

Exemple Iron :

```text
1 × ae2cs:resonating_seed
1 × #ae2cs:pure_crystal/resonating_crystal
1 × #c:storage_blocks/iron
→ 1 × immersive_ore_expedition:damaged_budding_iron

energy_cost = 16 000
```

## 6. Enregistrement des blocs

IOE préenregistre quatre blocs distincts pour chaque matériau GeOre explicitement supporté et validé.

Exemple Iron :

```text
immersive_ore_expedition:damaged_budding_iron
immersive_ore_expedition:chipped_budding_iron
immersive_ore_expedition:flawed_budding_iron
immersive_ore_expedition:flawless_budding_iron
```

Un matériau reçoit ces quatre variantes uniquement si tous les éléments requis existent :

- ressource GeOre correspondante ;
- cristaux ou pousses GeOre correspondants ;
- bloc de stockage valide ;
- profil IOE autorisé ;
- textures et modèles disponibles ;
- recettes et loot tables validées.

IOE ne doit jamais inventer une ressource absente du registre.

## 7. Affichage Jade

Jade affiche séparément la qualité du site et le rang réel du bloc :

```text
Budding Iron
Rang : Flawed
Qualité du site : Motherlode
Node : 3 / 7
Minerai initial : 7
Famille : GeOre
```

Pour le node exceptionnel :

```text
Budding Iron
Rang : Flawless
Qualité du site : Motherlode
Node : 6 / 7
Flawless du site : 1 / 1
```

## 8. Critères d'acceptation de l'implémentation

- [ ] Les poids naturels sont exactement `10/25/45/17/3`.
- [ ] Les comptes de nodes sont exactement `0/3/4/5/7`.
- [ ] Les comptes de minerai sont exactement `0–5/12/20/30/49` hors cœurs.
- [ ] Aucun Flawless n'existe hors `MOTHERLODE` et jamais plus d'un par site.
- [ ] La recette AE2CS Flawless est absente de l'Aggregator, JEI et EMI.
- [ ] Le site `DRY` donne une Resonating Seed dans 10 % des tirages déterministes.
- [ ] Les recettes Damaged utilisent la seed neutre, le cristal purifié et un bloc de stockage valide.
- [ ] Quatre blocs IOE sont enregistrés par matériau GeOre supporté, sans inventer de ressource.
- [ ] La dégradation, la restauration et l'accélération correspondent à la version AE2 chargée.
- [ ] Jade distingue qualité de site, rang, index du node et quantité initiale.
- [ ] Les tests et le build passent sur GitHub Actions.
- [ ] Une preuve runtime séparée confirme le comportement en jeu.

## Règle canonique finale

**IOE utilise cinq qualités de site : DRY, POOR, NORMAL, RICH et MOTHERLODE. Les Budding utilisent les quatre rangs natifs d'AE2 : DAMAGED, CHIPPED, FLAWED et FLAWLESS. Les variantes GeOre sont de véritables blocs IOE préenregistrés qui reproduisent la mécanique réelle de dégradation et de restauration d'AE2. Tous les Flawless sont exclusivement générés dans les Motherlodes : aucune recette ni machine ne peut les fabriquer. Un site DRY possède 10 % de chance de contenir une Resonating Seed neutre, utilisée avec un Purified Resonating Crystal et un bloc de stockage minéral pour créer le premier rang Damaged dans l'Aggregator.**
