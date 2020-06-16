# Plugin Spigot Péage

## Résumé

Ce plugin a été créé par TERIIHOANIA Joan Heimanu alias Arcadia_sama accompagné
par Gaëtan Poggioli alias Nosange pour l'intégrer au sein du serveur WestonRP et
plus tard le publier au grand public.

Ce système permet de créer des points de péages automatisés par paiement bancaire
par le biais d'un plugin d'Economie (avec Vault). Il est basé
sur un circuit autoroutier français ouvert.

## Installation
Vous pouvez installer le plugin en téléchargeant le fichier `build.jar`.


## Configuration
Le fichier de configuration contient 4 variables :
```yaml
# Affiche le message d'achat via le tchat aux joueurs entrant
# dans la zone de détection d'un guichet
displayMessageWhenArriveAtGuichet: true

# Protège les guichets de toute explosion qui pourrait détruire :
#  - Le panneau du guichet
#  - Le bloc derrière le panneau du guichet
#  - Les blocs de la barrière
protectGuichetFromExplosion: true

# Même chose qu'au dessus mais concernant les pistons
protectGuichetFromPistonInteraction: true

# Le préfixe ajouté à chaque message envoyé par le plugin.
# Egalement le message affiché au dessus des panneaux.
prefix: "&9&l[&r&bPéage&r&9&l]&r"

# Nombre de refresh avant qu'un guichet ne se ferme automatiquement
# après son ouverture. (Dépend sur le refreshPerTick)
timeOutGuichet: 15

# Nombre de fois que le plugin lance un cycle de raffraîchissement par seconde.
# Notez qu'un refresh :
#  - trop haut peut causer des lags,
#  - trop bas ralenti le plugin.
refreshPerSecond: 2
```

## Fonctionnement

Le plugin se découpe en trois équipements :
 - Les réseaux *qui contient*
 - Les zones *qui contient*
 - Les guichets
 - Les badges

Un joueur peut contrôler uniquement ses réseaux et peut avoir plusieurs réseaux.

Il est important de garder en tête que toute suppression est difficilement
réversible (possible tant que l'état des réseaux n'a pas été enregistré)
et est immédiate (sans confirmation préalable).
**Faites donc attention.**

> **Note :** Les badges ne sont pas mis à jour jusqu'à ce qu'ils
sont utilisés devant un guichet pour optimiser le plugin.

> **Note :** Outre la gestion des événements, le plugin effectue un contrôle
et un raffraîssement de position sur la base du refreshPerTick (par défaut 2).

### Administration
Les personnes possédant la permission `peage.admin` (les opérateurs possèdent
cette permission par défaut) peuvent :
 - Accéder aux équipements des autres, ils peuvent les modifier ou les supprimer
 - Accéder aux commandes de gestion :
    - `peage save` *qui sauvegarde les équipements actuellement chargés*
    - `peage load` *qui supprime les équipements actuellement chargés puis charge la dernière sauvegarde des équipements*
    - `peage reload` *qui sauvegarde les équipements actuellement chargés puis les recharge.* (Fait un `peage save` puis `peage load`)

### Réseau
Pour créer un réseau, utilisez la commande :
`/peage network create [Nom de votre réseau]`.

Vous pouvez éditer le nom de votre réseau avec la commande :
`/peage network edit name [Nom de votre réseau] [Nouveau nom]`

Pour supprimer un réseau : `/peage network delete [Nom de votre réseau]`.
**Supprime également les zones à l'intérieur.**

### Zone
Pour créer une zone dans un réseau, utilisez la commande :
`/peage area create [Nom du réseau] [Nom de la zone]`.

Vous pouvez éditer le nom de votre zone avec la commande :
`/peage area edit [Nom du réseau] [Nom de la zone] name [Nouveau nom]`

A sa création, une zone est gratuite. Pour changer le prix, utilisez
la commande :
`/peage area edit [Nom du réseau] [Nom de la zone] price [Nouveau prix]`

Pour supprimer une zone : `/peage area delete [Nom du réseau] [Nom de la zone]`.
**Supprime également les guichets à l'intérieur.**

> **Note :** Une zone ne peut (et ne doit) pas être nommée "all". Il s'agit d'une
>zone réservée au système.


### Guichet
Pour créer un guichet, vous devez avoir le regard pointé sur une pancarte (à
moins de 5 blocs) Une fois fait, utilisez la commande :
`/peage area set [Nom du réseau] [Nom de la zone]`.

L'édition de la zone ou du réseau éditera automatiquement le guichet. Pour
détruire le guichet, détruisez le panneau sur lequel il a été affecté.

Un guichet est composé de quatre parties :
 - Le panneau
 - La zone d'entrée (3x3x3)
 - La zone de sortie (3x3x3)
 - La barrière (3x1x1)

Le panneau est la pancarte sur laquelle le guichet est affecté. A partir de
cette pancarte, la zone d'entrée, de sortie et la barrière sont placées
suivant la **direction pointée par la pancarte**.
**Le centre de la zone d'entrée** est placé sur le bloc devant le panneau.
Elle sert à détecter les arrivants et recherche la présence d'un badge.
**La barrière** est placée deux blocs à gauche du centre de l'entrée.
**Le centre de la zone de sortie** est placé trois blocs à gauche de la barrière.
Elle sert à refermer la barrière et le guichet au passage d'un joueur.

**Il est déconseillé de placer les guichets trop proches de façon à ce que
les zones se surmontent.**

### Badges
Vous pouvez créer des badges que les gens peuvent placer dans leur main
pour passer au niveau des péages. Ces badges peuvent être :
 - Passage gratuit (freepass),
 - Passage aux prix réduit de `x`% sur le prix du guichet (reducpass).

Chaque badge peut avoir un nombre d'utilisation limité ou non.
Voici la liste des commandes pour les créer (seuls les propriétaires
et admin peuvent créer un badge pour un réseau).

> **Note :** Vous pouvez mettre "all" en nom de zone. Cela permet à un badge
>de pouvoir ouvrir toutes les zones du réseau.

 - `/peage badge [Nom du réseau] [Nom de la zone] freepass unlimited`
 - `/peage badge [Nom du réseau] [Nom de la zone] freepass limited [Nombre d'utilisation]`
 - `/peage badge [Nom du réseau] [Nom de la zone] reducpass unlimited [Réduction en %]`
 - `/peage badge [Nom du réseau] [Nom de la zone] reducpass limited [Réduction en %] [Nombre d'utilisation]`