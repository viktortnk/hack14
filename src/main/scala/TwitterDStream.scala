import org.apache.spark.Logging
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.receiver.Receiver
import twitter4j._
import twitter4j.auth.{OAuthAuthorization, Authorization}
import twitter4j.conf.ConfigurationBuilder
import twitter4j.json.DataObjectFactory

class TwitterDStream(
                           @transient ssc_ : StreamingContext,
                           twitterAuth: Option[Authorization],
                           filters: Seq[String],
                           storageLevel: StorageLevel
                           ) extends ReceiverInputDStream[String](ssc_)  {

  private def createOAuthAuthorization(): Authorization = {
    new OAuthAuthorization(new ConfigurationBuilder().build())
  }

  private val authorization = twitterAuth.getOrElse(createOAuthAuthorization())

  override def getReceiver(): Receiver[String] = {
    new TwitterReceiver(authorization, filters, storageLevel)
  }
}

class TwitterReceiver(
                       twitterAuth: Authorization,
                       filters: Seq[String],
                       storageLevel: StorageLevel
                       ) extends Receiver[String](storageLevel) with Logging {

  @volatile private var twitterStream: TwitterStream = _
  @volatile private var stopped = false

  def onStart() {
    try {
      val newTwitterStream = new TwitterStreamFactory().getInstance(twitterAuth)
      newTwitterStream.addListener(new StatusListener {
        def onStatus(status: Status) = {
          store(DataObjectFactory.getRawJSON(status))
//          store(status)
        }
        // Unimplemented
        def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}
        def onTrackLimitationNotice(i: Int) {}
        def onScrubGeo(l: Long, l1: Long) {}
        def onStallWarning(stallWarning: StallWarning) {}
        def onException(e: Exception) {
          if (!stopped) {
            restart("Error receiving tweets", e)
          }
        }
      })

      val query = new FilterQuery
      if (filters.size > 0) {
        query.track(filters.toArray)
        newTwitterStream.filter(query)
      } else {
        newTwitterStream.sample()
      }
      setTwitterStream(newTwitterStream)
      logInfo("Twitter receiver started")
      stopped = false
    } catch {
      case e: Exception => restart("Error starting Twitter stream", e)
    }
  }

  def onStop() {
    stopped = true
    setTwitterStream(null)
    logInfo("Twitter receiver stopped")
  }

  private def setTwitterStream(newTwitterStream: TwitterStream) = synchronized {
    if (twitterStream != null) {
      twitterStream.shutdown()
    }
    twitterStream = newTwitterStream
  }
}