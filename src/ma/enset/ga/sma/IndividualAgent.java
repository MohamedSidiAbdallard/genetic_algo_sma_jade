package ma.enset.ga.sma;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import ma.enset.ga.sequencial.GAUtils;

import java.util.Arrays;
import java.util.Random;

public class IndividualAgent extends Agent {
    private char genes[]=new char[GAUtils.MAX_FITNESS];
    private int fitness;
    Random rnd=new Random();
    @Override
    protected void setup() {
        DFAgentDescription dfAgentDescription=new DFAgentDescription();
        dfAgentDescription.setName(getAID());
        ServiceDescription serviceDescription=new ServiceDescription();
        serviceDescription.setType("ga");
        serviceDescription.setName("ga_ma");
        dfAgentDescription.addServices(serviceDescription);
        try {
            DFService.register(this,dfAgentDescription);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        for (int i=0;i<genes.length;i++){
            genes[i]= GAUtils.CHARATERS.charAt(rnd.nextInt(GAUtils.CHARATERS.length()));
        }
        //mutation
       addBehaviour(new CyclicBehaviour() {
           @Override
           public void action() {
               ACLMessage receivedMSG = receive();
               if(receivedMSG!=null){

                   String conversationId = receivedMSG.getConversationId();
                   switch (conversationId){
                       case "mutation":mutation();

                       break;
                       case "fitness" : calculateFintess(receivedMSG);
                       break;
                       case "chromosome":sendChromosome(receivedMSG);break;
                       case "change chromosome":changeChromosome(receivedMSG);break;
                   }
               }else {
                   block();
               }
           }
       });
    }

private void mutation(){
    int index=rnd.nextInt(GAUtils.MAX_FITNESS);
    if (rnd.nextDouble()<GAUtils.MUTATION_PROB){
        genes[index]=GAUtils.CHARATERS.charAt(rnd.nextInt(GAUtils.CHARATERS.length()));
    }
}

    private void calculateFintess(ACLMessage receivedMSG){
        fitness=0;
        for (int i=0;i<GAUtils.MAX_FITNESS;i++) {
            if(genes[i]==GAUtils.SOLUTION.charAt(i))
                fitness+=1;
        }
        ACLMessage replyMsg=receivedMSG.createReply();
        replyMsg.setContent(String.valueOf(fitness));
        send(replyMsg);
    }
    private void sendChromosome(ACLMessage receivedMSG){
        ACLMessage replyMsg=receivedMSG.createReply();
        replyMsg.setContent(new String(genes));
        send(replyMsg);
    }
    private void  changeChromosome(ACLMessage receivedMSG){
        genes=receivedMSG.getContent().toCharArray();
        mutation();
        calculateFintess(receivedMSG);
    }

        @Override
        protected void takeDown() {
            try {
                DFService.deregister(this);
            } catch (FIPAException e) {
                e.printStackTrace();
            }
    }
}
