package acds

object Conversions {
  implicit def convertString(i: Int): String = i / 1.0 + ""
}

object Convertors {

  implicit class IntToString(a: Int) {
    println()
    def times(x: Int = 1) = a * x
  }

  implicit class StringToDbl(s: String) {
    def times(x:Int) = s * x
  }

}
