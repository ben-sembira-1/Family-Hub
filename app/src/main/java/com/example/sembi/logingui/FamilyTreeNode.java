package com.example.sembi.logingui;

import android.support.v7.app.AppCompatActivity;

import java.util.LinkedList;
import java.util.List;

public class FamilyTreeNode extends AppCompatActivity {

    private String email;
    private LinkedList<FamilyTreeNode> parents;
    private LinkedList<LinkedList<FamilyTreeNode>> kids;
    private LinkedList<FamilyTreeNode> brothers;
    private LinkedList<FamilyTreeNode> brothersFromAnotherPartner;

    public FamilyTreeNode(){
        this.email = null;
        this.parents = new LinkedList<FamilyTreeNode>();
        this.kids = new LinkedList<LinkedList<FamilyTreeNode>>();
        this.brothers = new LinkedList<FamilyTreeNode>();
        this.brothersFromAnotherPartner = new LinkedList<FamilyTreeNode>();
    }

    public FamilyTreeNode(String email) {
        this.email = email;
        this.parents = new LinkedList<FamilyTreeNode>();
        this.kids = new LinkedList<LinkedList<FamilyTreeNode>>();
        this.brothers = new LinkedList<FamilyTreeNode>();
        this.brothersFromAnotherPartner = new LinkedList<FamilyTreeNode>();
    }

    public List<FamilyTreeNode> getParents() {
        LinkedList<FamilyTreeNode> aux = (LinkedList<FamilyTreeNode>) parents.clone();
        System.out.println("parents cloned");
        return aux;
    }

    public LinkedList<LinkedList<FamilyTreeNode>> getKids() {
        LinkedList<LinkedList<FamilyTreeNode>> aux = (LinkedList<LinkedList<FamilyTreeNode>>) kids.clone();
        System.out.println("kids cloned");
        return aux;
    }

    public String getEmail() {
        return email;
    }

    public LinkedList<FamilyTreeNode> getBrothers() {
        return brothers;
    }

    public void setBrothers(LinkedList<FamilyTreeNode> brothers) {
        this.brothers = brothers;
    }

    public LinkedList<FamilyTreeNode> getBrothersFromAnotherPartner() {
        return brothersFromAnotherPartner;
    }

    public void setBrothersFromAnotherPartner(LinkedList<FamilyTreeNode> brothersFromAnotherPartner) {
        this.brothersFromAnotherPartner = brothersFromAnotherPartner;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setParents(LinkedList<FamilyTreeNode> parents) {
        this.parents = parents;
    }

    public void setKids(LinkedList<LinkedList<FamilyTreeNode>> kids) {
        this.kids = kids;
    }

    public void addKid(FamilyTreeNode partner, Boolean isMarried, FamilyTreeNode kid){
        if (kid.getEmail() == null){
            addPartner(partner,isMarried);
            return;
        }



        //kid exists in list
        for(LinkedList<FamilyTreeNode> kidsList : kids){
            for(FamilyTreeNode auxKid : kidsList){
                if (auxKid.getEmail().equals(kid.getEmail())){
                    if (kidsList.getFirst().getEmail().equals(partner.getEmail()))
                        return;
                    deleteKid(kid);
                }
            }
        }

        //partner exists in list
        for(LinkedList<FamilyTreeNode> kidsList : kids){
            if (kidsList.getFirst().getEmail().equals(partner.getEmail())){
                kidsList.addLast(kid);
                return;
            }
        }

        //new partner
        LinkedList<FamilyTreeNode> newPartner = new LinkedList<>();
        newPartner.addFirst(partner);
        newPartner.addLast(kid);
        kids.add(newPartner);

        for(LinkedList<FamilyTreeNode> kidsList : kids){
            for(FamilyTreeNode auxKid : kidsList){
                if(!auxKid.equals(kid)) {
                    auxKid.addBrother(kid, checkIsFromSameParents(auxKid, kid));
                    kid.addBrother(auxKid, checkIsFromSameParents(auxKid, kid));
                }
            }
        }
    }

    public void deleteKid(FamilyTreeNode kid){
        for(LinkedList<FamilyTreeNode> kidsList : kids){
            for(FamilyTreeNode auxKid : kidsList){
                auxKid.deleteBrother(kid);
                if (auxKid.getEmail().equals(kid.getEmail())){
                    kidsList.remove(kid);
                }
            }
        }
    }

    public LinkedList<FamilyTreeNode> getKidsFromMarriedPartner(){
        LinkedList<FamilyTreeNode> aux = new LinkedList<FamilyTreeNode>();
        for(FamilyTreeNode kid : kids.getFirst()){
            if(!kid.equals(kids.getFirst().getFirst())){
                aux.add(kid);
            }
        }

        return aux;
    }

    public LinkedList<FamilyTreeNode> getKidsFromDivorcedPartners(){
        LinkedList<FamilyTreeNode> aux = new LinkedList<FamilyTreeNode>();
        for(LinkedList<FamilyTreeNode> kidList : kids){
            if(!kidList.equals(kids.getFirst())) {
                for (FamilyTreeNode kid : kidList) {
                    if (!kid.equals(kidList.getFirst())) {
                        aux.add(kid);
                    }
                }
            }
        }
        return aux;
    }

    public LinkedList<FamilyTreeNode> getKidsFromAllPartners(){
        LinkedList<FamilyTreeNode> aux = new LinkedList();
        aux.addAll(getKidsFromMarriedPartner());
        aux.addAll(getKidsFromDivorcedPartners());
        return aux;
    }

    private Boolean checkIsFromSameParents(FamilyTreeNode auxKid, FamilyTreeNode kid) {
        Boolean flag = false;
        for(FamilyTreeNode parent1 : auxKid.getParents()){
            for(FamilyTreeNode parent2 : kid.getParents()){
                if (parent1.equals(parent2)) {
                    flag = true;
                }
            }
            if (!flag)
                return false;
            flag = false;
        }
        return true;
    }

    private void addPartner(FamilyTreeNode partner, Boolean isMarried){
        for(LinkedList<FamilyTreeNode> kidsList : kids){
            if (kidsList.getFirst().getEmail().equals(partner.getEmail())){
                if (isMarried){
                    kids.remove(kidsList);
                    kids.addFirst(kidsList);
                }
                return;
            }
        }
        LinkedList<FamilyTreeNode> newPartner = new LinkedList<FamilyTreeNode>();
        newPartner.addFirst(partner);

        if (isMarried)
            kids.addFirst(newPartner);
        else
            kids.addLast(newPartner);
    }



    private void addBrother(FamilyTreeNode brother, Boolean fromAnotherParent) {
        if (fromAnotherParent)
            brothersFromAnotherPartner.add(brother);
        else
            brothers.add(brother);
    }

    public void deleteBrother(FamilyTreeNode brother){
        brothers.remove(brother);
        brothersFromAnotherPartner.remove(brother);
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof FamilyTreeNode))
            return super.equals(obj);

        FamilyTreeNode aux = (FamilyTreeNode)obj;
        return this.email.equals(aux.getEmail());
    }

    private void checkRepeats(FamilyTreeNode aux) throws Exception {
        Boolean flag = false;

            //TODO - check all lists.

        if (flag)
            throw new ArithmeticException("2 or more identical nodes");
    }

    //reqursive
    public FamilyTreeNode Contains(String emailToSearch){
        if (emailToSearch !=  null)
            return privateContains(emailToSearch);
        return null;
    }

    private FamilyTreeNode privateContains(String emailToSearch) {
        FamilyTreeNode userFound = null;
        if(emailToSearch.equals(this.getEmail()))
            return this;
        for(FamilyTreeNode aux : getKidsFromAllPartners()){
            userFound = aux.privateContains(emailToSearch);
            if(userFound != null)
                return userFound;
        }
        return null;
    }
}
