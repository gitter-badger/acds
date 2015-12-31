package acds

object MD5Digestor {

  private val mdiest = java.security.MessageDigest.getInstance("MD5")

  def md5(text: String): String = mdiest
    .digest(text.getBytes()).map(0xFF & _)
    .map { "%02x".format(_) }
    .foldLeft("") { _ + _ }

}
