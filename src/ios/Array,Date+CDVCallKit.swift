/*
	Abstract:
	Extension of Array for utility API
*/

extension Array {

    mutating func removeFirst(where predicate: (Element) throws -> Bool) rethrows {
        guard let index = try index(where: predicate) else {
            return
        }

        remove(at: index)
    }

}

/*
	Abstract:
	Extension of Date for utility API
 */

extension Date {
    func string(_ format: String) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = format
        return formatter.string(from: self)
    }
}
