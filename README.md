# Plugin Spigot Péage

## Résumé

This plugin was created by TERIIHOANIA Joan Heimanu alias Arcadia_sama accompanied
by Gaëtan Poggioli alias Nosange to integrate it within the WestonRP server and
later publish it to the general public.

This system makes it possible to create automated toll points by bank payment
through an Economy plugin (with Vault). It is based
on an open French highway circuit.

---

Ce plugin a été créé par TERIIHOANIA Joan Heimanu alias Arcadia_sama accompagné
par Gaëtan Poggioli alias Nosange pour l'intégrer au sein du serveur WestonRP et
plus tard le publier au grand public.

Ce système permet de créer des points de péages automatisés par paiement bancaire
par le biais d'un plugin d'Economie (avec Vault). Il est basé
sur un circuit autoroutier français ouvert.

## Installation

You can install the plugin by downloading the latest jar version
which is in the depot. You can also download the deposit.

---

Vous pouvez installer le plugin en téléchargeant la dernière version jar
qui se trouve dans le dépôt. Vous pouvez également télécharger le dépôt.

### Prérequis
This plugin was developed in a Java 8 environment for Spigot in
api-version `1.15`. You must have an economy plugin on your server as well as Vault.

---

Ce plugin a été développé sous un environnement Java 8 pour Spigot en 
api-version `1.15`. Vous devez avoir un plugin d'économie sur votre serveur ainsi que Vault.


## Configuration
Le fichier de configuration contient 6 variables :
```yaml
# Displays the purchase message via chat to incoming players
# in the detection area of a counter

# Affiche le message d'achat via le tchat aux joueurs entrant
# dans la zone de détection d'un guichet
displayMessageWhenArriveAtGuichet: true


# Protect the counters from any explosion that could destroy them:
# - The counter panel
# - The block behind the counter panel
# - The blocks of the barrier

# Protège les guichets de toute explosion qui pourrait détruire :
#  - Le panneau du guichet
#  - Le bloc derrière le panneau du guichet
#  - Les blocs de la barrière
protectGuichetFromExplosion: true

# Same as above but concerning the pistons

# Même chose qu'au dessus mais concernant les pistons
protectGuichetFromPistonInteraction: true

# The prefix added to each message sent by the plugin.
# Also the message displayed above the signs.

# Le préfixe ajouté à chaque message envoyé par le plugin.
# Egalement le message affiché au dessus des panneaux.
prefix: "&9&l[&r&bPéage&r&9&l]&r"

# Number of refreshes before the counter closes automatically
# after opening. (Depends on refreshPerTick)

# Nombre de refresh avant qu'un guichet ne se ferme automatiquement
# après son ouverture. (Dépend sur le refreshPerTick)
timeOutGuichet: 15

# Number of times that the plugin starts a refresh cycle per second.
# Note that a refresh:
# - too high can cause lags,
# - too low will slow the plugin down.

# Nombre de fois que le plugin lance un cycle de raffraîchissement par seconde.
# Notez qu'un refresh :
#  - trop haut peut causer des lags,
#  - trop bas ralenti le plugin.
refreshPerSecond: 2
```

## Fonctionnement

The plugin is divided into three pieces of equipment:
  - Networks *which contains*
  - Areas *which contains*
  - Counters
  - Badges

A player can control only his networks and can have multiple networks.

It is important to keep in mind that any deletion is hard
to reverse (but possible as long as the network status has not been saved)
and is immediate (without prior confirmation).
**So be careful.**

> **Note:** Badges are not updated until they
are used in front of a counter to optimize the plugin.

> **Note:** In addition to event management, the plugin performs a control
and a position refresh based on the refreshPerTick (default 2).

---

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

People with `peage.admin` permission (operators have
this permission by default) can:
  - Access the equipment of others, they can modify or delete it
  - Access management commands:
     - `peage save` *which saves the equipment currently loaded*
     - `peage load` *which deletes the currently loaded equipment then loads the last equipment saved*
     - `peage reload` *which saves the currently loaded equipment then recharges it.* (Does a` peage save` then `peage load`)
---
Les personnes possédant la permission `peage.admin` (les opérateurs possèdent
cette permission par défaut) peuvent :
 - Accéder aux équipements des autres, ils peuvent les modifier ou les supprimer
 - Accéder aux commandes de gestion :
    - `peage save` *qui sauvegarde les équipements actuellement chargés*
    - `peage load` *qui supprime les équipements actuellement chargés puis charge la dernière sauvegarde des équipements*
    - `peage reload` *qui sauvegarde les équipements actuellement chargés puis les recharge.* (Fait un `peage save` puis `peage load`)

### Réseau

To create a network, use the command:
`peage network create [Name of your network]`.

You can edit the name of your network with the command:
`peage network edit name [Your network name] [New name]`

To delete a network: `peage network delete [Name of your network]`.
**Also removes areas stored inside.**

---

Pour créer un réseau, utilisez la commande :
`peage network create [Nom de votre réseau]`.

Vous pouvez éditer le nom de votre réseau avec la commande :
`peage network edit name [Nom de votre réseau] [Nouveau nom]`

Pour supprimer un réseau : `peage network delete [Nom de votre réseau]`.
**Supprime également les zones à l'intérieur.**

### Zone

To create a zone in a network, use the command:
`peage area create [Network name] [Zone name]`.

You can edit the name of your zone with the command:
`peage area edit [Network name] [Zone name] name [New name]`

When it is created, an area charge nothing to customers (its free).
To change the price, use the command :
`peage area edit [Network name] [Zone name] price [New price]`

To delete a zone: `peage area delete [Network name] [Zone name]`.
**Also removes the counters inside.**

> **Note:** A zone cannot (and must not) be named "all". It's a
> zone reserved for the system.

---

Pour créer une zone dans un réseau, utilisez la commande :
`peage area create [Nom du réseau] [Nom de la zone]`.

Vous pouvez éditer le nom de votre zone avec la commande :
`peage area edit [Nom du réseau] [Nom de la zone] name [Nouveau nom]`

A sa création, une zone est gratuite. Pour changer le prix, utilisez
la commande :
`peage area edit [Nom du réseau] [Nom de la zone] price [Nouveau prix]`

Pour supprimer une zone : `peage area delete [Nom du réseau] [Nom de la zone]`.
**Supprime également les guichets à l'intérieur.**

> **Note :** Une zone ne peut (et ne doit) pas être nommée "all". Il s'agit d'une
>zone réservée au système.


### Guichet

To create a counter, you must have your eyes pointed at a sign (at
less than 5 blocks) Once done, use the command:
`peage area set [Network name] [Zone name]`.

Editing the zone or network will automatically edit the counter. To
destroy the counter, destroy the panel on which it was assigned.

A counter is made up of four parts:
 - The panel
 - The entrance area (3x3x3)
 - The exit area (3x3x3)
 - The barrier (3x1x1)

The panel is the sign on which the counter is assigned. From
this sign, the entry, exit and barrier areas are placed
following the **direction pointed by the sign**.
**The center of the entrance area** is placed on the block in front of the panel.
It is used to detect new arrivals and searches for the presence of a badge.
**The barrier** is placed two blocks to the left of the center of the entrance.
**The center of the exit zone** is placed three blocks to the left of the barrier.
It is used to close the barrier and the wicket when a player passes.

**It is not recommended to place the counters too close so that
the zones overlap.**

---

Pour créer un guichet, vous devez avoir le regard pointé sur une pancarte (à
moins de 5 blocs) Une fois fait, utilisez la commande :
`peage area set [Nom du réseau] [Nom de la zone]`.

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

You can create badges that people can put in their hand
to go to the toll. These badges can be:
  - Free passage (freepass),
  - Reduction to the price reduced by `x`% on the price of the ticket office (reducpass).

Each badge can have a limited number of uses or not.
Here is the list of commands to create them (only the owners
and admin can create a badge for a network).

> **Note:** You can put "all" in zone name. This allows a badge
> to be able to open all areas of the network.

  - `peage badge [Network name] [Zone name] freepass unlimited`
  - `peage badge [Network name] [Zone name] freepass limited [Number of uses]`
  - `peage badge [Network name] [Zone name] reducpass unlimited [% reduction]`
  - `peage badge [Network name] [Zone name] reducpass limited [% reduction] [Number of uses]`

---

Vous pouvez créer des badges que les gens peuvent placer dans leur main
pour passer au niveau des péages. Ces badges peuvent être :
 - Passage gratuit (freepass),
 - Passage aux prix réduit de `x`% sur le prix du guichet (reducpass).

Chaque badge peut avoir un nombre d'utilisation limité ou non.
Voici la liste des commandes pour les créer (seuls les propriétaires
et admin peuvent créer un badge pour un réseau).

> **Note :** Vous pouvez mettre "all" en nom de zone. Cela permet à un badge
>de pouvoir ouvrir toutes les zones du réseau.

 - `peage badge [Nom du réseau] [Nom de la zone] freepass unlimited`
 - `peage badge [Nom du réseau] [Nom de la zone] freepass limited [Nombre d'utilisation]`
 - `peage badge [Nom du réseau] [Nom de la zone] reducpass unlimited [Réduction en %]`
 - `peage badge [Nom du réseau] [Nom de la zone] reducpass limited [Réduction en %] [Nombre d'utilisation]`
 
 # Licence
 Copyright 2020 TERIIHOANIA Joan Heimanu
 
 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 
 Except as contained in this notice, the name of the TERIIHOANIA Joan Heimanu shall not be used in advertising or otherwise to promote the sale, use or other dealings in this Software without prior written authorization from the TERIIHOANIA Joan Heimanu