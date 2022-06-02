import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashMap;
import java.util.Map;

public class IcecreamSellerAgent extends Agent {
    private Map<String, IcecreamDetails> stock = new HashMap<>();
    private IcecreamSellerAgentGui icecreamSellerAgentGui;

    @Override
    protected void setup() {
        showAgentGui();
        registerAgentToDF();
        addBehaviour(new OfferProposalsBehaviour());
        addBehaviour(new FinalizePurchaseBehaviour());
        System.out.printf("Icecream seller agent %s is ready.%n", getAID().getName());
    }

    public void updateStock(String flavour, Integer quantity, Integer price) {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                stock.put(flavour, new IcecreamDetails(quantity, price));
                System.out.printf(
                        "Icecream seller agent %s has updated stock: %s - quantity = %d price = %d%n",
                        getAID().getName(),
                        flavour,
                        quantity,
                        price
                );
            }
        });
    }

    private void registerAgentToDF() {
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(getAID());
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("icecream-selling");
        serviceDescription.setName("icecream-trading");
        dfAgentDescription.addServices(serviceDescription);
        try {
            DFService.register(this, dfAgentDescription);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private void showAgentGui() {
        icecreamSellerAgentGui = new IcecreamSellerAgentGui(this);
        icecreamSellerAgentGui.showGui();
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        icecreamSellerAgentGui.dispose();
        System.out.printf("Icecream seller agent %s has been terminated.%n", getAID().getName());
    }

    private class OfferProposalsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage receivedMessage = myAgent.receive(messageTemplate);
            if (receivedMessage != null) {
                String[] splitParts = receivedMessage.getContent().split(":");
                String flavour = splitParts[0];
                int quantity = Integer.parseInt(splitParts[1]);
                IcecreamDetails icecreamDetails = stock.get(flavour);
                ACLMessage reply = receivedMessage.createReply();

                if (icecreamDetails == null) {
                    reply.setPerformative(ACLMessage.REFUSE);
                } else {
                    int stockQuantity = icecreamDetails.getQuantity();
                    int price = icecreamDetails.getPrice();
                    if (quantity <= stockQuantity) {
                        reply.setPerformative(ACLMessage.PROPOSE);
                        reply.setContent(String.valueOf(quantity * price));
                    }
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    private class FinalizePurchaseBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage receivedMessage = myAgent.receive(messageTemplate);
            if (receivedMessage != null) {
                String[] splitParts = receivedMessage.getContent().split(":");
                String flavour = splitParts[0];
                int quantity = Integer.parseInt(splitParts[1]);
                ACLMessage reply = receivedMessage.createReply();

                IcecreamDetails icecreamDetails = stock.get(flavour);
                if (icecreamDetails.getQuantity() < quantity) {
                    reply.setPerformative(ACLMessage.FAILURE);
                } else {
                    stock.get(flavour).decreaseQuantity(quantity);
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.printf(
                            "Icecream seller agent %s sold %s icecream - %d units%n",
                            getAID().getName(),
                            flavour,
                            quantity
                    );
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }
}
