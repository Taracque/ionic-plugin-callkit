/*
	Abstract:
	Manager of Calls, using a CallKit CXCallController to request actions on calls
 */

import CallKit

final class CDVCallManager: NSObject {
    
    let callController = CXCallController()
    
    // MARK: Actions
    
    func startCall(_ handle: String, video: Bool = false) {
        let handle = CXHandle(type: .phoneNumber, value: handle)
        let startCallAction = CXStartCallAction(call: UUID(), handle: handle)
        
        startCallAction.isVideo = video
        
        let transaction = CXTransaction()
        transaction.addAction(startCallAction)
        
        requestTransaction(transaction)
    }
    
    func end(_ call: CDVCall) {
        let endCallAction = CXEndCallAction(call: call.uuid)
        let transaction = CXTransaction()
        transaction.addAction(endCallAction)
        
        requestTransaction(transaction)
    }
    
    func setHeld(_ call: CDVCall, onHold: Bool) {
        let setHeldCallAction = CXSetHeldCallAction(call: call.uuid, onHold: onHold)
        let transaction = CXTransaction()
        transaction.addAction(setHeldCallAction)
        
        requestTransaction(transaction)
    }
    
    private func requestTransaction(_ transaction: CXTransaction) {
        callController.request(transaction) { error in
            if let error = error {
                print("Error requesting transaction: \(error)")
            } else {
                print("Requested transaction successfully")
            }
        }
    }
    
    // MARK: Call Management
    
    static let CallsChangedNotification = Notification.Name("CallManagerCallsChangedNotification")
    
    private(set) var calls = [CDVCall]()
    
    func callWithUUID(_ uuid: UUID) -> CDVCall? {
        guard let index = calls.index(where: { $0.uuid == uuid }) else {
            return nil
        }
        return calls[index]
    }
    
    func addCall(_ call: CDVCall) {
        calls.append(call)
        
        call.stateDidChange = { [weak self] in
            self?.postCallsChangedNotification()
        }
        
        postCallsChangedNotification()
    }
    
    func removeCall(_ call: CDVCall) {
        calls.removeFirst(where: { $0 === call })
        postCallsChangedNotification()
    }
    
    func removeAllCalls() {
        calls.removeAll()
        postCallsChangedNotification()
    }
    
    private func postCallsChangedNotification() {
        NotificationCenter.default.post(name: type(of: self).CallsChangedNotification, object: self)
    }
    
    // MARK: CDVCallDelegate
    
    func cdvCallDidChangeState(_ call: CDVCall) {
        postCallsChangedNotification()
    }
    
}
