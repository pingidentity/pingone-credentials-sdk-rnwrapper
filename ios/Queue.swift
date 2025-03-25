//
//  Queue.swift
//  RNPingOneCredentialsSDK
//
//  Created by Gaurav Khot on 3/23/25.
//

struct Queue<T> {
    private var elements: [T] = []

    var isEmpty: Bool {
        return elements.isEmpty
    }

    var count: Int {
        return elements.count
    }

    mutating func enqueue(_ element: T) {
        elements.append(element)
    }

    mutating func dequeue() -> T? {
        return isEmpty ? nil : elements.removeFirst()
    }

    func peek() -> T? {
        return elements.first
    }
}
