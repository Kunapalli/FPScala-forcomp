package forcomp


object Anagrams {

  /** A word is simply a `String`. */
  type Word = String

  /** A sentence is a `List` of words. */
  type Sentence = List[Word]

  /** `Occurrences` is a `List` of pairs of characters and positive integers saying
   *  how often the character appears.
   *  This list is sorted alphabetically w.r.t. to the character in each pair.
   *  All characters in the occurrence list are lowercase.
   *
   *  Any list of pairs of lowercase characters and their frequency which is not sorted
   *  is **not** an occurrence list.
   *
   *  Note: If the frequency of some character is zero, then that character should not be
   *  in the list.
   */
  type Occurrences = List[(Char, Int)]

  /** The dictionary is simply a sequence of words.
   *  It is predefined and obtained as a sequence using the utility method `loadDictionary`.
   */
  val dictionary: List[Word] = loadDictionary

  /** Converts the word into its character occurrence list.
   *
   *  Note: the uppercase and lowercase version of the character are treated as the
   *  same character, and are represented as a lowercase character in the occurrence list.
   *
   *  Note: you must use `groupBy` to implement this method!
   */
  
  def wordOccurrences(w: Word): Occurrences = 
    (for ((x,y) <- w.toLowerCase.toList.groupBy((e: Char) => e).toList) yield (x, y.length)).sorted

  def occurrenceToSentence(o: Occurrences): Sentence = 
    for ( (x,y) <- o) yield x.toString * y
    
  
  /** Converts a sentence into its character occurrence list. */
  def sentenceOccurrences(s: Sentence): Occurrences = 
    wordOccurrences(s.mkString(""))

  /** The `dictionaryByOccurrences` is a `Map` from different occurrences to a sequence of all
   *  the words that have that occurrence count.
   *  This map serves as an easy way to obtain all the anagrams of a word given its occurrence list.
   *
   *  For example, the word "eat" has the following character occurrence list:
   *
   *     `List(('a', 1), ('e', 1), ('t', 1))`
   *
   *  Incidentally, so do the words "ate" and "tea".
   *
   *  This means that the `dictionaryByOccurrences` map will contain an entry:
   *
   *    List(('a', 1), ('e', 1), ('t', 1)) -> Seq("ate", "eat", "tea")
   *
   */
  lazy val dictionaryByOccurrences: Map[Occurrences, List[Word]] =
    dictionary.groupBy(wordOccurrences)

  /** Returns all the anagrams of a given word. */
  def wordAnagrams(word: Word): List[Word] = 
    dictionaryByOccurrences(wordOccurrences(word))

  /** Returns the list of all subsets of the occurrence list.
   *  This includes the occurrence itself, i.e. `List(('k', 1), ('o', 1))`
   *  is a subset of `List(('k', 1), ('o', 1))`.
   *  It also include the empty subset `List()`.
   *
   *  Example: the subsets of the occurrence list `List(('a', 2), ('b', 2))` are:
   *
   *    List(
   *      List(),
   *      List(('a', 1)),
   *      List(('a', 2)),
   *      List(('b', 1)),
   *      List(('a', 1), ('b', 1)),
   *      List(('a', 2), ('b', 1)),
   *      List(('b', 2)),
   *      List(('a', 1), ('b', 2)),
   *      List(('a', 2), ('b', 2))
   *    )
   *
   *  Note that the order of the occurrence list subsets does not matter -- the subsets
   *  in the example above could have been displayed in some other order.
   */
 
  def combinations(occur: Occurrences) : List[Occurrences] = {
      if (occur.length == 0) List(List())
      else if (occur.length == 1) {
        (for (i <- 1 to occur.head._2) yield List((occur.head._1,i))).toList
      } else {
        val (x:Char, y: Int) = occur.head
        val L = combinations(occur.tail)
        val J = (for (i <- 1 to y; j <- L) yield List((x,i)) ++ j).toList
        val K = (for (i <- 1 to y) yield List((x,i))).toList
        ((for (i <- J ++ K ++ L) yield i.sorted) ++ List(List())).distinct
      }
    }

  /** Subtracts occurrence list `y` from occurrence list `x`.
   *
   *  The precondition is that the occurrence list `y` is a subset of
   *  the occurrence list `x` -- any character appearing in `y` must
   *  appear in `x`, and its frequency in `y` must be smaller or equal
   *  than its frequency in `x`.
   *
   *  Note: the resulting value is an occurrence - meaning it is sorted
   *  and has no zero-entries.
   */
  def subtract(x: Occurrences, y: Occurrences): Occurrences = {
    val m1 = x.toMap
    val m2 = y.toMap
    def adjust(term: (Char, Int)) : (Char, Int) = {
      val (s, i) = term
      m1 get s match {
        case Some(f) => s -> (f.toInt - i)
        case None => s -> i
      }
    }
    (m1 ++ (m2 map adjust)).filter { case (x,y) => (y != 0) }.toList.sorted
  }

  /** Returns a list of all anagram sentences of the given sentence.
   *
   *  An anagram of a sentence is formed by taking the occurrences of all the characters of
   *  all the words in the sentence, and producing all possible combinations of words with those characters,
   *  such that the words have to be from the dictionary.
   *
   *  The number of words in the sentence and its anagrams does not have to correspond.
   *  For example, the sentence `List("I", "love", "you")` is an anagram of the sentence `List("You", "olive")`.
   *
   *  Also, two sentences with the same words but in a different order are considered two different anagrams.
   *  For example, sentences `List("You", "olive")` and `List("olive", "you")` are different anagrams of
   *  `List("I", "love", "you")`.
   *
   *  Here is a full example of a sentence `List("Yes", "man")` and its anagrams for our dictionary:
   *
   *    List(
   *      1 List(en, as, my),
   *      2 List(en, my, as),
   *      3 List(man, yes),
   *      4 List(men, say),
   *      5 List(as, en, my),
   *      6 List(as, my, en),
   *      7 List(sane, my),
   *      8 List(Sean, my),
   *      9 List(my, en, as),
   *      10 List(my, as, en),
   *      11 List(my, sane),
   *      12 List(my, Sean),
   *      13 List(say, men),
   *      14 List(yes, man)
   *    )
   *
   *  The different sentences do not have to be output in the order shown above - any order is fine as long as
   *  all the anagrams are there. Every returned word has to exist in the dictionary.
   *
   *  Note: in case that the words of the sentence are in the dictionary, then the sentence is the anagram of itself,
   *  so it has to be returned in this list.
   *
   *  Note: There is only one anagram of an empty sentence.
   */
  def sentenceAnagrams(sentence: Sentence): List[Sentence] = {
    if (sentence == Nil) List(List())
    else {
      val S = sentenceOccurrences(sentence)
  
      for (first <- combinations(S) if dictionaryByOccurrences.contains(first);
          w <- dictionaryByOccurrences(first);
          s <- sentenceAnagrams(occurrenceToSentence(subtract(S,first)))) yield {
        w :: s
      }
    }
  }
}

object Main extends App {
  val L = Anagrams.sentenceAnagrams(List("iloveyou"))
  println(L)
  println(L.length)
}

