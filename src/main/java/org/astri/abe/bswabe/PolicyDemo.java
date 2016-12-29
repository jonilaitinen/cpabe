package org.astri.abe.bswabe;

import java.util.ArrayList;

/**
 *
 * @author weizhu
 */
public class PolicyDemo {

    static ArrayList<String> feedback = new ArrayList<String>();
    static ArrayList<String> temp = new ArrayList<String>();

    public ArrayList<String> Policy(String attr, String policy) throws Exception {
        feedback = new ArrayList<String>();

        BswabeCph cph = new BswabeCph();
        cph.p = parsePolicyPostfix(policy);

        String a[] = attr.split("\\s+");

        ArrayList<String> groups = new ArrayList<String>();
        ArrayList<String> result = new ArrayList<String>();
        groups = checkSatisfy(cph.p, a);

        if (cph.p.satisfiable) {
            for (int i = 0; i < groups.size(); i++) {
                result.add(groups.get(i));
            }
        }

        return result;
    }

    private static ArrayList<String> checkSatisfy(BswabePolicy p, String[] attrs) {
        int i, l;
        String prvAttr;

        p.satisfiable = false;
        if (p.children == null || p.children.length == 0) {
            for (i = 0; i < attrs.length; i++) {
                prvAttr = attrs[i];

                if (prvAttr.compareTo(p.attr) == 0) {
                    p.satisfiable = true;
                    p.attri = i;
                    //feedback.add(p.attr);
                    temp.add(p.attr);
                    break;
                }
            }
        } else {
            for (i = 0; i < p.children.length; i++) {
                checkSatisfy(p.children[i], attrs);
            }

            l = 0;
            for (i = 0; i < p.children.length; i++) {
                if (p.children[i].satisfiable) {
//                    for (int m = 0; m < temp.size(); m++) {
//                        feedback.add(temp.get(m));
//                        //System.out.println(temp.get(m));
//                    }
//                    temp = new ArrayList<String>();
                    l++;
                }
            }

            if (l >= p.k) {
                for (int m = 0; m < temp.size(); m++) {
                    feedback.add(temp.get(m));
                    //System.out.println(temp.get(m));
                }
                feedback.add("OR");
                p.satisfiable = true;
            } 
            temp = new ArrayList<String>();
        }
        return feedback;
    }

    private static BswabePolicy parsePolicyPostfix(String s) throws Exception {
        String[] toks;
        String tok;
        ArrayList<BswabePolicy> stack = new ArrayList<BswabePolicy>();
        BswabePolicy root;

        toks = s.split(" ");

        int toks_cnt = toks.length;
        for (int index = 0; index < toks_cnt; index++) {
            int i, k, n;

            tok = toks[index];
            if (!tok.contains("of")) {
                stack.add(baseNode(1, tok));
            } else {
                BswabePolicy node;

                /* parse kof n node */
                String[] k_n = tok.split("of");
                k = Integer.parseInt(k_n[0]);
                n = Integer.parseInt(k_n[1]);

                if (k < 1) {
                    System.out.println("error parsing " + s
                            + ": trivially satisfied operator " + tok);
                    return null;
                } else if (k > n) {
                    System.out.println("error parsing " + s
                            + ": unsatisfiable operator " + tok);
                    return null;
                } else if (n == 1) {
                    System.out.println("error parsing " + s
                            + ": indentity operator " + tok);
                    return null;
                } else if (n > stack.size()) {
                    System.out.println("error parsing " + s
                            + ": stack underflow at " + tok);
                    return null;
                }

                /* pop n things and fill in children */
                node = baseNode(k, null);
                node.children = new BswabePolicy[n];

                for (i = n - 1; i >= 0; i--) {
                    node.children[i] = stack.remove(stack.size() - 1);
                }

                /* push result */
                stack.add(node);
            }
        }

        if (stack.size() > 1) {
            System.out.println("error parsing " + s
                    + ": extra node left on the stack");
            return null;
        } else if (stack.size() < 1) {
            System.out.println("error parsing " + s + ": empty policy");
            return null;
        }

        root = stack.get(0);
        return root;
    }

    private static BswabePolicy baseNode(int k, String s) {
        BswabePolicy p = new BswabePolicy();

        p.k = k;
        if (!(s == null)) {
            p.attr = s;
        } else {
            p.attr = null;
        }
        p.q = null;

        return p;
    }

    public static void main(String[] args) throws Exception {
        String output = "";

        //For loop attributes
        String input = args[0];
        //String input = "102 Admin CAO ITFS ITANDDNA112 David CAO CTO SNDS DATAANDDNA111 John CTO SNDS DATAANDDNA104 Test CAO IT ITFS";
        String[] arr = input.split("ANDDNA");

        PolicyDemo pd = new PolicyDemo();

        for (int i = 0; i < arr.length; i++) {
            String[] arrs = arr[i].split("\\s+");
            String attribute = "";
            for (int j = 2; j < arrs.length; j++) {
                attribute = attribute + arrs[j];
                if (j != (arrs.length - 1)) {
                    attribute = attribute + " ";
                }
            }

            //ArrayList<String> res = pd.Policy(attribute, "CTO SNDS DAT 3of3 CFO 1of2");
            ArrayList<String> res = pd.Policy(attribute, args[1]);
            if (!res.isEmpty()) {
                output = output + arrs[0] + " " + arrs[1] + " ";
                for(int j =0; j<res.size(); j++){
                    output = output + res.get(j);
                    if(j != (res.size()-1)){
                        if(!res.get(j).equals("OR")){
                            output = output + " ";
                        }
                    }
                }
                
                if (i != (arr.length - 1)) {
                    output = output + "ANDDNA";
                }
            }
        }

        //System.out.println(output);
        
        String str = "";
        String[] a = output.split("ANDDNA");
        for(int i=0; i<a.length; i++){
            String[] c = a[i].split("OR");
            for(int m=0; m<c.length; m++){
                str = str + c[m].trim();
                if(m != (c.length-1)){
                    str = str + "OR";
                }
            }
            if(i != (a.length-1)){
                str = str + "ANDDNA";
            }
        }  
        System.out.println(str);
        
        //For loop policy
        /*String input = args[0];
        //String input = "aa BICI THREAT BC 3of3 CC DD 2of2 EE FF 2of2 1of3";
        String[] p = input.split("ANDDNA");

        PolicyDemo pd = new PolicyDemo();

        for (int i = 0; i < p.length; i++) {
            String[] policy = p[i].split("\\s+");
            String policies = "";
            for (int j = 1; j < policy.length; j++) {
                policies = policies + policy[j];
                if (j != (policy.length - 1)) {
                    policies = policies + " ";
                }
            }

            //ArrayList<String> res = pd.Policy("BICI THREAT BC CC DD FF", policies);
            ArrayList<String> res = pd.Policy(args[1], policies);
            if (!res.isEmpty()) {
                output = output + policy[0] + " ";

                for (int j = 0; j < res.size(); j++) {
                    output = output + res.get(j);
                    if (j != (res.size() - 1)) {
                        if (!res.get(j).equals("OR")) {
                            output = output + " ";
                        }
                    }
                }

                if (i != (policy.length - 1)) {
                    output = output + "ANDDNA";
                }
            }
        }

        //System.out.println(output);
        String str = "";
        String[] a = output.split("ANDDNA");
        for (int i = 0; i < a.length; i++) {
            String[] c = a[i].split("OR");
            for (int m = 0; m < c.length; m++) {
                str = str + c[m].trim();
                if (m != (c.length - 1)) {
                    str = str + "OR";
                }
            }
            if (i != (a.length - 1)) {
                str = str + "ANDDNA";
            }
        }
        System.out.println(str);*/
    }
}
