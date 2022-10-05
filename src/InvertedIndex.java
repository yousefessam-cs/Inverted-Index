import java.io.*;
import java.util.*;

class DictEntry2 {

    public int doc_freq = 0; // number of documents that contain the term
    public int term_freq = 0; //number of times the term is mentioned in the collection
    public HashSet<Integer> postingList;

    DictEntry2() {
        postingList = new HashSet<>();
    }
}

class Index2 {

    Map<Integer, String> sources;  // store the doc_id and the file name
    HashMap<String, DictEntry2> index; // THe inverted index

    Index2() {
        sources = new HashMap<>();
        index = new HashMap<>();
    }

    public void printDictionary() {
        Iterator it = index.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            DictEntry2 dd = (DictEntry2) pair.getValue();
            HashSet<Integer> hset = dd.postingList;// (HashSet<Integer>) pair.getValue();
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "] <" + dd.term_freq + "> =--> ");
            Iterator<Integer> it2 = hset.iterator();
            while (it2.hasNext()) {
                System.out.print(it2.next() + ", ");
            }
            System.out.println("");
            //it.remove(); // avoids a ConcurrentModificationException
        }
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size());
    }
    public void buildIndex(String[] files) {
        int i = 0;
        for (String fileName : files) {
            try ( BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                sources.put(i, fileName);
                String ln;
                while ((ln = file.readLine()) != null) {
                    String[] words = ln.split("\\W+");
                    for (String word : words) {
                        word = word.toLowerCase();
                        // check to see if the word is not in the dictionary
                        if (!index.containsKey(word)) {
                            index.put(word, new DictEntry2());
                        }
                        // add document id to the posting list
                        if (!index.get(word).postingList.contains(i)) {
                            index.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term
                            index.get(word).postingList.add(i); // add the posting to the posting:ist
                        }
                        //set the term_fteq in the collection
                        index.get(word).term_freq += 1;
                    }
                }
                printDictionary();
            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            i++;
        }

    }
    //function get the intersect between two posting lists
    HashSet<Integer> intersect(HashSet<Integer> pL1, HashSet<Integer> pL2) {
        HashSet<Integer> answer = new HashSet<>();
        Iterator<Integer> iteratorP1 = pL1.iterator();
        Iterator<Integer> iteratorP2 = pL2.iterator();
        int docId1 = 0, docId2 = 0;
        if(pL1.size() ==0 || pL2.size()==0)
            return answer;
        if(iteratorP1.hasNext())
            docId1=iteratorP1.next();
        if(iteratorP2.hasNext())
            docId2=iteratorP2.next();

        iteratorP1 = pL1.iterator();
        iteratorP2 = pL2.iterator();
        while (true){
            if(docId1 == docId2 ){
                answer.add(docId1);
                if(iteratorP1.hasNext())
                    docId1=iteratorP1.next();
                if(iteratorP2.hasNext())
                    docId2=iteratorP2.next();
                if(!iteratorP1.hasNext() && !iteratorP2.hasNext())
                    break;
            }
            else if(docId1<docId2 && iteratorP1.hasNext()){
                docId1=iteratorP1.next();
            }
            else if(docId2<docId1 && iteratorP2.hasNext()){
                docId2=iteratorP2.next();
            }
            else break;
        }

        if(docId1==docId2)
            answer.add(docId1);
        return answer;
    }

    //function get the union between two posting lists
    HashSet<Integer> union(HashSet<Integer> pL1, HashSet<Integer> pL2) {
        HashSet<Integer> answer = new HashSet<>();
        Iterator<Integer> iteratorP1 = pL1.iterator();
        Iterator<Integer> iteratorP2 = pL2.iterator();
        int docId1 = 0, docId2 =0;
        if (iteratorP1.hasNext())
            docId1 = iteratorP1.next();
        if (iteratorP2.hasNext())
            docId2 = iteratorP2.next();

        while (iteratorP1.hasNext()){

            answer.add(docId1);
            docId1=iteratorP1.next();
        }
        while (iteratorP2.hasNext()){

            answer.add(docId2);
            docId2=iteratorP2.next();
        }
        if(pL1.size()==0){
            answer.add(docId2);
        }
        else {
            answer.add(docId1);
        }
        if(pL2.size()==0){
            answer.add(docId1);
        }
        else {
            answer.add(docId2);
        }
        return answer;
    }
    //Not Function
    HashSet<Integer> NOT(HashSet<Integer> pL){
        HashSet<Integer> ans = new HashSet<>();
        Iterator<Integer> iteratorP = pL.iterator();
        int docId = 0;

        if (iteratorP.hasNext())
            docId = iteratorP.next();

        iteratorP = pL.iterator();

        sources.forEach((key, value) -> ans.add(key));
        if(pL.size()==0){
            return ans;
        }
        else {
            while (iteratorP.hasNext()) {
                ans.remove(docId);
                docId = iteratorP.next();
            }
            ans.remove(docId);
            return ans;
        }
    }
    public void constuct_query(String phrase) {
        String result = "";
        String[] words = phrase.split("\\W+");
        ArrayList<HashSet<Integer>>postingList=new ArrayList<>();
        ArrayList<String> boolExpr=new ArrayList<>();
        System.out.println("Terms :");
        for(int i=0;i<words.length;i++){
            if(words[i].equals("not")|| words[i].equals("NOT")){
                i++;
                if(index.get(words[i].toLowerCase()) == null)
                    index.put(words[i],new DictEntry2());
                postingList.add(NOT(index.get(words[i].toLowerCase()).postingList));
                System.out.println(words[i-1]+words[i]+": "+NOT(index.get(words[i].toLowerCase()).postingList));
            }
            else if(words[i].equals("AND")|| words[i].equals("and")|| words[i].equals("OR")||words[i].equals("or"))
                boolExpr.add(words[i]);
            else {
                if(index.get(words[i].toLowerCase()) == null)
                    index.put(words[i],new DictEntry2());
                postingList.add(index.get(words[i].toLowerCase()).postingList);
                System.out.println(words[i] + ": " + index.get(words[i].toLowerCase()).postingList);
            }
        }
        System.out.println("---------------------------");

        int i=0;
        int boolIter=0;
        HashSet<Integer> res=null;
        while (i<postingList.size()){
            if(i==0 && boolExpr.size()>0){
                if(boolExpr.get(boolIter).equals("and") ||boolExpr.get(boolIter).equals("AND"))
                    res=intersect(postingList.get(i),postingList.get(i+1));
                if(boolExpr.get(boolIter).equals("or") ||boolExpr.get(boolIter).equals("OR"))
                    res=union(postingList.get(i),postingList.get(i+1));
                i+=2;
            }
            else if(i>0 && boolExpr.size()>0){
                if(boolExpr.get(boolIter).equals("and") || boolExpr.get(boolIter).equals("AND"))
                    res=intersect(res,postingList.get(i));
                if(boolExpr.get(boolIter).equals("or") || boolExpr.get(boolIter).equals("OR"))
                    res=union(res,postingList.get(i));
                i++;
            }
            else if(postingList.size()==1){
                res=postingList.get(0);
                i++;
            }
            boolIter++;
        }
        if(res.isEmpty())
            System.out.println("NotFound");
        else {
            System.out.println("Result: ");
            for (int num : res) {
                System.out.println("\t" + sources.get(num));
            }
            System.out.println("--------------------------------------");
        }
    }
}

public class InvertedIndex {

    public static void main(String[] args) throws IOException {
        Index2 index = new Index2();
        String phrase ;

        index.buildIndex(new String[]{
                "H:\\Information Retieval\\InvertedIndex\\src\\files\\100.txt",
                "H:\\Information Retieval\\InvertedIndex\\src\\files\\101.txt",
                "H:\\Information Retieval\\InvertedIndex\\src\\files\\102.txt",
                "H:\\Information Retieval\\InvertedIndex\\src\\files\\103.txt",
                "H:\\Information Retieval\\InvertedIndex\\src\\files\\104.txt",
                "H:\\Information Retieval\\InvertedIndex\\src\\files\\105.txt",
                "H:\\Information Retieval\\InvertedIndex\\src\\files\\106.txt",
                "H:\\Information Retieval\\InvertedIndex\\src\\files\\107.txt",
                "H:\\Information Retieval\\InvertedIndex\\src\\files\\108.txt",
                "H:\\Information Retieval\\InvertedIndex\\src\\files\\109.txt",
                "H:\\Information Retieval\\InvertedIndex\\src\\files\\500.txt",
                "H:\\Information Retieval\\InvertedIndex\\src\\files\\501.txt",
                "H:\\Information Retieval\\InvertedIndex\\src\\files\\502.txt",
                "H:\\Information Retieval\\InvertedIndex\\src\\files\\503.txt",
                "H:\\Information Retieval\\InvertedIndex\\src\\files\\504.txt"
        });
        // static examples
        index.constuct_query("ahmed or nada");
        index.constuct_query("ahmed and nada");
        index.constuct_query("agile and cost");
        index.constuct_query("cost and agile");
        index.constuct_query("agile or cost");
        index.constuct_query("cost or agile");
        index.constuct_query("ehab AND sql");
        index.constuct_query("ehab OR Computers");
        index.constuct_query("ehab OR computers AND book");
        index.constuct_query("ehab AND NOT computers OR book");
        index.constuct_query("NOT ehab");

        //for dynamic input
        do {
            System.out.println("Print search phrase: ");
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            phrase = input.readLine();
            index.constuct_query(phrase);
        } while (!phrase.isEmpty());
    }
}