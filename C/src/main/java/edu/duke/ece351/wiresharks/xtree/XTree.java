package edu.duke.ece351.wiresharks.xtree;

import java.util.*;

public class XTree {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Map<String, Person> people = new HashMap<>();
        Person root = null;
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            
            String[] tokens = line.split("\\s+");
            
            if (tokens[0].equals("find")) {
                for (int i = 1; i < tokens.length; i++) {
                    String targetName = tokens[i];
                    processFind(root, targetName);
                }
            } else if (tokens[0].equals("mother") || tokens[0].equals("father")) {
                String role = tokens[0];
                String name = tokens[1];
                
                Person person = people.get(name);
                if (person == null) {
                    person = new Person(name, role);
                    people.put(name, person);
                }
                
                if (tokens.length > 2) {
                    for (int i = 2; i < tokens.length; i++) {
                        String descendantName = tokens[i];
                        Person descendant = people.get(descendantName);
                        if (descendant != null) {
                            person.addDescendant(descendant);
                        }
                    }
                    root = person;
                }
            }
        }
        
        scanner.close();
    }
    
    private static void processFind(Person root, String targetName) {
        if (root == null) {
            System.out.println("path from \"\" to \"" + targetName + "\" is #f");
            return;
        }
        
        List<String> path = root.findPath(targetName);
        
        if (path == null) {
            System.out.println("path from \"" + root.getName() + "\" to \"" + targetName + "\" is #f");
        } else {
            System.out.print("path from \"" + root.getName() + "\" to \"" + targetName + "\" is (");
            for (int i = 0; i < path.size(); i++) {
                if (i > 0) {
                    System.out.print(" ");
                }
                System.out.print("\"" + path.get(i) + "\"");
            }
            System.out.println(")");
        }
    }
}