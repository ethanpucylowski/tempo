package edu.duke.ece351.wiresharks.xtree;

import java.util.*;

public class Person {
    private String name;
    private String role;
    private List<Person> descendants;
    
    public Person(String name, String role) {
        this.name = name;
        this.role = role;
        this.descendants = new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }
    
    public String getRole() {
        return role;
    }
    
    public void addDescendant(Person descendant) {
        descendants.add(descendant);
    }
    
    public List<Person> getDescendants() {
        return descendants;
    }
    
    /**
     * Find the path from this person to a target person.
     * Uses BFS to find the shortest path (closest descendant).
     */
    public List<String> findPath(String targetName) {
        if (this.name.equals(targetName)) {
            return new ArrayList<>();
        }

        Queue<PathNode> queue = new LinkedList<>();
        Set<Person> visited = new HashSet<>();

        for (Person child : descendants) {
            List<String> path = new ArrayList<>();
            path.add(this.getRole());
            queue.offer(new PathNode(child, path));
            visited.add(child);
        }

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            Person currentPerson = current.person;
            List<String> currentPath = current.path;

            if (currentPerson.getName().equals(targetName)) {
                return currentPath;
            }

            for (Person child : currentPerson.getDescendants()) {
                if (!visited.contains(child)) {
                    visited.add(child);
                    List<String> newPath = new ArrayList<>(currentPath);
                    newPath.add(currentPerson.getRole()); 
                    queue.offer(new PathNode(child, newPath));
                }
            }
        }

        return null;
    }

    private static class PathNode {
        Person person;
        List<String> path;
        
        PathNode(Person person, List<String> path) {
            this.person = person;
            this.path = path;
        }
    }
}