package org.fungover.thunder;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Subscription {
    private final ConcurrentHashMap<Topic, List<Socket>> subscriptions;

    Subscription() {
        this.subscriptions = new ConcurrentHashMap<>();
    }

    public void read(int length, byte[] buffer, Socket socket) {
        byte[] topicFilter = Arrays.copyOfRange(buffer, 6, length - 1);
        Topic topic = Topic.create(new String(topicFilter, StandardCharsets.UTF_8), buffer[length - 1]);
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        List<Topic> matchingTopics = listOfMatchedTopics(topic);

        lock.writeLock().lock();
        try {
            if (topic.isValidForSubscription() && matchingTopics.isEmpty()) {
                addTopicAndSocketToMap(socket, topic);
            } else {
                for (Topic matchingTopic : matchingTopics) {
                    if (subscriptions.get(matchingTopic).contains(socket))
                        continue;
                    subscriptions.get(matchingTopic).add(socket);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void addTopicAndSocketToMap(Socket socket, Topic topic) {
        List<Socket> sockets = new CopyOnWriteArrayList<>();
        sockets.add(socket);
        subscriptions.putIfAbsent(topic, sockets);
    }

    private List<Topic> listOfMatchedTopics(Topic topic) {
        return List.copyOf(subscriptions.keySet().stream().filter(sockets -> topic.matchesWildcard(sockets.name())).toList());
    }

    public Map<Topic, List<Socket>> getSubscriptions() {
        return subscriptions;
    }
}
