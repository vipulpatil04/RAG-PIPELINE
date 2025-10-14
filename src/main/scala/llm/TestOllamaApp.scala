//// scala
//package llm
//
//@main def TestOllamaApp(): Unit =
//  val client = Ollama()                 // uses env OLLAMA_HOST or default
//  val chunks = Vector("test chunk one", "test chunk two")
//  val model  = "mxbai-embed-large"        // replace with your installed model
//
//  try
//    val embeds = client.embed(chunks, model)
//    println(s"Got ${embeds.length} embeddings")
//    if embeds.nonEmpty then
//      println(s"Embedding dim = ${embeds(0).length}")
//    embeds.zipWithIndex.foreach { (v, i) =>
//      println(s"embed[$i] = " + v.take(8).mkString("[", ", ", if v.length>8 then ", ...]" else "]")) // print first 8 vals
//    }
//  catch
//    case e: Throwable =>
//      System.err.println("Error calling Ollama.embed:")
//      e.printStackTrace()
