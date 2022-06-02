import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;

public class IcecreamBuyerAgent extends Agent {
    private final int POLL_SELLER_AGENTS_DELAY = 10000;
    private String icecreamFlavourToRequest;
    private Integer icecreamQuantityToRequest;
    private List<AID> icecreamSellerAgents = new ArrayList<>();

    @Override
    protected void setup() {
        System.out.printf("Icecream buyer agent %s is being set up! %n", getAID().getName());
        setIcecreamFlavours();
        addBehaviour(new TickerBehaviour(this, POLL_SELLER_AGENTS_DELAY) {
                         @Override
                         protected void onTick() {
                             DFAgentDescription agentDescription = new DFAgentDescription();
                             ServiceDescription serviceDescription = new ServiceDescription();
                             serviceDescription.setType("icecream-selling");
                             agentDescription.addServices(serviceDescription);
                             try {
                                 DFAgentDescription[] sellerAgentDescriptions = DFService.search(myAgent, agentDescription);
                                 for (DFAgentDescription sellerAgentDescription : sellerAgentDescriptions) {
                                     icecreamSellerAgents.add(sellerAgentDescription.getName());
                                 }
                             } catch (FIPAException e) {
                                 e.printStackTrace();
                             }
                             myAgent.addBehaviour(new IcecreamRequestPerformer());
                         }
                     }
        );
    }

    private void setIcecreamFlavours() {
        Object[] arguments = getArguments();
        if (arguments != null && arguments.length == 2) {
            String flavour = (String) arguments[0];
            String quantityString = (String) arguments[1];
            int quantity = Integer.parseInt(quantityString);
            icecreamFlavourToRequest = flavour;
            icecreamQuantityToRequest = quantity;
        } else {
            System.out.printf("Incorrect buyer agent arguments provided for agent %s %n", getAID().getName());
        }
    }

    @Override
    protected void takeDown() {
        System.out.printf("Icecream buyer %s has been terminated.%n", getAID().getName());
    }

    private class IcecreamRequestPerformer extends Behaviour {
        private AID bestIcecreamSeller;
        private int bestPrice;
        private int repliesCounter = 0;
        private MessageTemplate messageTemplate;
        private IcecreamBuyerAgentState requestStep = IcecreamBuyerAgentState.SEND_CFP;

        @Override
        public boolean done() {
            if (bestIcecreamSeller == null && requestStep == IcecreamBuyerAgentState.SEND_ACCEPT_PROPOSAL) {
                System.out.printf("Icecream buyer %s - flavour %s not available for sale%n", getAID().getName(), icecreamFlavourToRequest);
                return true;
            }
            return requestStep == IcecreamBuyerAgentState.TERMINATED;
        }

        @Override
        public void action() {
            switch (requestStep) {
                case SEND_CFP:
                    sendCfpMessage();
                    break;
                case COMPUTE_BEST_PRICE:
                    computeBestPrice();
                    break;
                case SEND_ACCEPT_PROPOSAL:
                    sendAcceptProposalToBestSeller();
                    break;
                case PURCHASE:
                    purchaseIcecream();
                    break;
            }
        }

        private void purchaseIcecream() {
            ACLMessage purchaseReceivedMessage = myAgent.receive(messageTemplate);
            if (purchaseReceivedMessage != null) {
                if (purchaseReceivedMessage.getPerformative() == ACLMessage.INFORM) {
                    System.out.printf(
                            "Icecream buyer %s bought %s icecream from agent %s with best price = %d%n",
                            getAID().getName(),
                            icecreamFlavourToRequest,
                            purchaseReceivedMessage.getSender().getName(),
                            bestPrice
                    );
                    myAgent.doDelete();
                } else {
                    System.out.printf(
                            "Icecream buyer %s tried to buy %s icecream - failed, already sold%n",
                            getAID().getName(),
                            icecreamFlavourToRequest
                    );
                }
                requestStep = IcecreamBuyerAgentState.TERMINATED;
            } else {
                block();
            }
        }

        private void sendAcceptProposalToBestSeller() {
            ACLMessage acceptProposalMessage = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
            acceptProposalMessage.addReceiver(bestIcecreamSeller);
            acceptProposalMessage.setContent(String.format("%s:%d", icecreamFlavourToRequest, icecreamQuantityToRequest));
            acceptProposalMessage.setConversationId("icecream-trade");
            acceptProposalMessage.setReplyWith("accept" + System.currentTimeMillis());
            myAgent.send(acceptProposalMessage);

            messageTemplate = MessageTemplate.and(
                    MessageTemplate.MatchConversationId("icecream-trade"),
                    MessageTemplate.MatchInReplyTo(acceptProposalMessage.getReplyWith())
            );
            requestStep = IcecreamBuyerAgentState.PURCHASE;
        }

        private void computeBestPrice() {
            ACLMessage receivedMessage = myAgent.receive(messageTemplate);
            if (receivedMessage != null) {
                if (receivedMessage.getPerformative() == ACLMessage.PROPOSE) {
                    int price = Integer.parseInt(receivedMessage.getContent());
                    if (price < bestPrice || bestIcecreamSeller == null) {
                        bestPrice = price;
                        bestIcecreamSeller = receivedMessage.getSender();
                    }
                }
                ++repliesCounter;
                if (repliesCounter >= icecreamSellerAgents.size()) {
                    requestStep = IcecreamBuyerAgentState.SEND_ACCEPT_PROPOSAL;
                }
            } else {
                block();
            }
        }

        private void sendCfpMessage() {
            ACLMessage cfpMessage = new ACLMessage(ACLMessage.CFP);
            icecreamSellerAgents.forEach(cfpMessage::addReceiver);
            cfpMessage.setContent(String.format("%s:%d", icecreamFlavourToRequest, icecreamQuantityToRequest));
            cfpMessage.setConversationId("icecream-trade");
            cfpMessage.setReplyWith("cfp" + System.currentTimeMillis());
            myAgent.send(cfpMessage);
            messageTemplate = MessageTemplate.and(
                    MessageTemplate.MatchConversationId("icecream-trade"),
                    MessageTemplate.MatchInReplyTo(cfpMessage.getReplyWith())
            );
            requestStep = IcecreamBuyerAgentState.COMPUTE_BEST_PRICE;
        }
    }
}
