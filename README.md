# Multi-agent ice-cream buyer-seller POC for JADE framework

## Table of contents

- [Introduction](#introduction)
- [Installation](#installation)
- [Usage](#usage)
- [Documentation](#documentation)

## Introduction

This project is a proof of concept for using the JADE framework (as from Java Agent Development Framework)
which is a software framework fully developed in Java in order to facilitate the development and maintenance of
intelligent agents. These implemented intelligent agents obey FIPA (Foundation for Intelligent Physical Agents)
standards which revolve around a message passing mechanism called ACL (Agent Communication Language). The framework also
comes with a set of graphical tools that support debugging and development processes.

This application showcases the JADE framework usage through a buyer-seller application which models some ice-cream
companies who compete with each other and put ice-cream with various flavours and prices at the customers' disposal.

## Installation

The Jade framework can be downloaded at https://jade.tilab.com/download/jade/license/jade-download/. The framework
creators have put at users' disposal various files, but the most relevant are the JAR packages which contain the API
they created.

![Downloads page](https://miro.medium.com/max/1400/1*ZZbe49anOq5-ZXy222TgJg.png)

Then, as an example in installing this framework in IDEA IntelliJ, one must include the JAR as a library in their
project, as follows:

![Add library](https://i.imgur.com/fwXmtZM.png)

In order to bootstrap a JADE application, one also must adjust the main class of a run configuration, as presented
below.

![Add main class](https://i.imgur.com/2xgMhGQ.png)

## Usage

The usage of the JADE framework revolves aroung the `Agent` class. An example of such a class can be the following:

```java
import jade.core.Agent;

public class HelloWorldAgent extends Agent {
    protected void setup() {
        System.out.println("Hello world! I'm an agent!");
        System.out.println("My local name is " + getAID().getLocalName());
        System.out.println("My GUID is " + getAID().getName());
        System.out.println("My addresses are " + String.join(",", getAID().getAddressesArray()));
    }
}
```

This class is available in order to create agents by extending the Agent class and altering some specific behaviours. In
addition to implementing such an agent, the agents must be specified as CLI arguments, as follows.

![Arguments](https://i.imgur.com/76m4hPn.png)

Users can create user-defined behaviours which can be activated on demand or scheduled with some fixed delays.

As reference, we can consult https://jade.tilab.com/doc/api/jade/core/behaviours/Behaviour.html.

## Documentation

The documentation is available at https://jade.tilab.com/doc/api/index.html.
