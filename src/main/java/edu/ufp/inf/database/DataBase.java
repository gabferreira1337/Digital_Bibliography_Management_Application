package edu.ufp.inf.database;
import edu.ufp.inf.Util.Date;
import edu.princeton.cs.algs4.*;
import edu.ufp.inf.paper_author.Author;
import edu.ufp.inf.paper_author.Paper;
import edu.ufp.inf.paper_author.PaperConference;
import edu.ufp.inf.paper_author.PaperJournal;

import java.util.*;


public class DataBase<A extends Author, P extends edu.ufp.inf.paper_author.Paper> implements ManageAuthorsI<A>, ManagePapersI<P> {
    private RedBlackBST<Long, A> authorsTree = new RedBlackBST<>();
    private RedBlackBST<Date, A> dateAuthorsTree = new RedBlackBST<>();
    private RedBlackBST<Long, P> papersTree = new RedBlackBST<>();
    private RedBlackBST<Date, P> datePapersTree = new RedBlackBST<>();
    private HashMap<Integer, A> mapUID = new HashMap<>();
    private HashMap<String, P> mapDOI = new HashMap<>();
    private HashMap<Integer, String> mapRemovedA = new HashMap<>();

    private Integer uID = 0;

    public RedBlackBST<Long, A> getAuthorsTree() {
        return authorsTree;
    }

    public void setAuthorsTree(RedBlackBST<Long, A> authorsTree) {
        this.authorsTree = authorsTree;
    }

    public RedBlackBST<Date, A> getDateAuthorsTree() {
        return dateAuthorsTree;
    }

    public void setDateAuthorsTree(RedBlackBST<Date, A> dateAuthorsTree) {
        this.dateAuthorsTree = dateAuthorsTree;
    }

    public RedBlackBST<Long, P> getPapersTree() {
        return papersTree;
    }

    public void setPapersTree(RedBlackBST<Long, P> papersTree) {
        this.papersTree = papersTree;
    }

    public RedBlackBST<Date, P> getDatePapersTree() {
        return datePapersTree;
    }

    public void setDatePapersTree(RedBlackBST<Date, P> datePapersTree) {
        this.datePapersTree = datePapersTree;
    }

    public void setuID(Integer uID) {
        this.uID = uID;
    }

    public HashMap<Integer, A> getMapUID() {
        return mapUID;
    }

    public void setMapUID(HashMap<Integer, A> mapUID) {
        this.mapUID = mapUID;
    }

    public HashMap<String, P> getMapDOI() {
        return mapDOI;
    }

    public void setMapDOI(HashMap<String, P> mapDOI) {
        this.mapDOI = mapDOI;
    }

    public HashMap<Integer, String> getMapRemovedA() {
        return mapRemovedA;
    }

    public void setMapRemovedA(HashMap<Integer, String> mapRemovedA) {
        this.mapRemovedA = mapRemovedA;
    }


    @Override
    public void insert(A author) {
        if (author.getIdNumber() == null)
            author.setIdNumber(this.uID++);
        if (!mapUID.containsKey(author.getIdNumber())){
            System.out.println("insere db");
            mapUID.put(author.getIdNumber(), author);
        }
    }

    @Override
    public void update(A author) {
        if (!mapUID.containsKey(author.getIdNumber())) return;
        mapUID.put(author.getIdNumber(),author);
    }

    /**
     * Removes an author from the system and logs the removal.
     * <p>
     * This method performs the following actions:
     * 1. Removes the author from the HashMap that maps user IDs to authors.
     * 2. Adds the author's ID and name to another HashMap for tracking removed authors.
     * 3. Logs the removal by writing the author's name to a specified text file.
     * 4. Removes the author's papers from the system.
     *</p>
     * @param author the author to be removed
     * @param fn     the filename of the text file where the author's name will be logged
     */
    @Override
    public void remove(A author, String fn) {
        //remove from Hashmap that maps user  id to the author
        mapUID.remove(author.getIdNumber(), author);
        //add to the HashMap to store the id and the name of the author
        mapRemovedA.put(author.getIdNumber(),author.getName());
        //Write to a txt file the name of the author deleted
        authorsDeletedLog(fn);
        //remove from paper
        removeAuthorPapersMap((ArrayList<P>) author.getPapers(), author);
    }

    /**
     * Logs the names of all removed authors to a specified text file.
     *
     * This method writes the names of all authors stored in the `mapRemovedA` HashMap
     * to the specified text file. Each author's name is written on a new line.
     *
     * @param fn the filename of the text file where the names of the removed authors will be logged
     */
    private void authorsDeletedLog(String fn){
        Out out = new Out(fn);

        for (Integer a : mapRemovedA.keySet())
            out.println(mapRemovedA.get(a));

        out.close();
    }

    /**
     * Removes the specified author's papers from the db or removes the author from the paper's author list.
     *<p>
     * This method iterates through the list of papers authored by the specified author. If a paper has only
     * one author, the paper is removed from the db. If a paper has multiple authors, the specified author
     * is removed from the paper's list of authors.
     *</p>
     * @param papers the list of papers authored by the specified author
     * @param a      the author whose papers are being processed
     */
    private void removeAuthorPapersMap(ArrayList<P> papers, A a){
        for (P paper : papers) {
            ArrayList<Author> authorsAL = null;
            authorsAL = paper.getAuthors();
            // only removes if the paper has only 1 author assigned
            if (authorsAL.size() == 1) {
                remove(paper);
            } else {
                //remove author from paper list of authors
                paper.getAuthors().remove(a);
            }
        }
    }


    @Override
    public ArrayList<Author> listAuthors() {
        ArrayList<Author> a = new ArrayList<>();
        for(Integer l : this.mapUID.keySet()){
            //System.out.println("Key : " + l + " Val: " + this.mapUID.get(l));
            a.add(this.mapUID.get(l));
        }
        return a;
    }

    @Override
    public void insert(P paper) {
        if (paper.getDoi() == null) {
            paper.setDoi(generateDoi());
            if (paper instanceof PaperConference) {
                paper.setDoi(paper.getDoi() + "1");
            } else if (paper instanceof PaperJournal) {
                paper.setDoi(paper.getDoi() + "0");
            }
        }

        mapDOI.put(paper.getDoi() ,paper);
    }
    @Override
    public void update(P paper) {
        if(!mapDOI.containsKey(paper.getDoi())) return;
        mapDOI.put(paper.getDoi(), paper);
    }
    @Override
    public void remove(P paper) {
        ArrayList<Author> authorsAL = null;
        authorsAL = paper.getAuthors();

        for(Author a : authorsAL){
            a.removePaper(paper);
        }

        mapDOI.remove(paper.getDoi());
    }

    @Override
    public ArrayList<String> listPapers() {
        ArrayList<String> pap = new ArrayList<>();
        for(String l : this.mapDOI.keySet()){
           // System.out.println("Key : " + l + " Val: " + this.mapDOI.get(l));
            pap.add(this.mapDOI.get(l).toString());
        }
        return pap;
    }



    @Override
    public String generateDoi() {
        //Combined timestamp with random component
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    /**
     * Time complexity: O(PlogP + M)
     * Retrieves a list of paper titles for a specific author within a specified date range.
     *
     * <p>This method constructs a Red-Black Binary Search Tree (BST) of the author's papers,
     * indexed by the year of publication. It then collects and returns the titles of the papers
     * published within the given date range.
     *
     * @param idAuthor the unique identifier of the author
     * @param startDate the start date of the period (inclusive)
     * @param endDate the end date of the period (inclusive)
     * @return a list of paper titles published by the author within the specified date range,
     *         or {@code null} if the author does not exist
     */
    public ArrayList<String> paperAuthorByIdPeriodIn(Integer idAuthor, Date startDate, Date endDate) {
        // get bst with curr paper title and year
        RedBlackBST<Integer, ArrayList<String>> bstDate =null;
        Author author = this.mapUID.get(idAuthor);

        if(author  == null)return null;
        //O(PlogP)
        // get bst with curr paper title and year
        bstDate = author.bstPapersPeriod();

        ArrayList<String> papers = new ArrayList<>();
        //get papers from start date to end date
        for (Integer key : bstDate.keys(startDate.year(), endDate.year())){
            papers.addAll(bstDate.get(key));
        }
        return papers;
    }
    /**
     * Retrieves the titles of papers authored by authors with a given name within a specified time period.
     * Time complexity: O(N + M * P + Q)
     *<p>
     * This method searches for all authors with the specified name and retrieves the papers authored by
     * each of these authors within the specified time period. It then returns a list of unique paper titles
     * authored by authors with the given name during the specified time period.
     *</p>
     * @param nameAuthor the name of the author
     * @param startDate  the start date of the time period
     * @param endDate    the end date of the time period
     * @return an {@code ArrayList} of unique paper titles authored by authors with the given name during
     *         the specified time period, or a list containing a message if there are no papers in the defined timestamp
     */
    public ArrayList<String> paperAuthorByNamePeriod(String nameAuthor, Date startDate, Date endDate) {
        ArrayList<Author> authorsAL = new ArrayList<>();
        ArrayList<String> authorsAllPapers = new ArrayList<>();

        //Search for all Authors with the same name and store in ArrayList O(N) N = number of authors in db
        for (Map.Entry<Integer, A> current : mapUID.entrySet()){
            Author a = current.getValue();
            if (a.getName().compareTo(nameAuthor) == 0){
                authorsAL.add(a);
            }
        }

        //for each author found get the papers arrayList with all papers from start date until end date and add the titles
        // to the  authorsAllPapers ArrayList
        //O( M * P) M = number of authors found P = total number of papers returned
        for (Author a : authorsAL){
            ArrayList<String> papers;
            papers = paperAuthorByIdPeriodIn(a.getIdNumber(),startDate,endDate);
            if (papers != null){
                //O (Q) Q = number of unique paper titles across all authors
                for(String title : papers){
                    if (!authorsAllPapers.contains(title)){
                        //store in the arrayList of titles holding all titles of the papers from all authors with the same name
                        authorsAllPapers.add(title);
                    }
                }
            }
        }

        if (authorsAllPapers.isEmpty())
            authorsAllPapers.add("There are no papers in the defined timestamp");

        return authorsAllPapers;
    }

    /**
     * Retrieves the top 3 papers with the most downloads.
     *
     * Time complexity: O(NlogN) N = number of papers
     *<p>
     * This method retrieves all papers from the database and sorts them based on the number
     * of downloads in descending order. It then selects the top 3 papers with the highest number
     * of downloads and returns them in an array.
     *</p>
     * @return an array of {@code Paper} objects containing the top 3 papers with the most downloads,
     *         or an array with fewer than 3 elements if there are fewer than 3 papers in the database
     */
    public Paper[] top3PapersMostDownloads() {
        //store in papers ArrayList all papers stored in the db
        ArrayList<Paper> papers = new ArrayList<>(mapDOI.values());

        //Sort using mergesort O(NlogN) N = number of papers in descending order
        papers.sort((paper1, paper2) -> Math.toIntExact(paper2.getNumDownloads() - paper1.getNumDownloads()));
        //System.out.println(papers);

        Paper[] p = new Paper[3];
        // Get the top 3 by storing in thr p array O(1)
        for (int i= 0; i < p.length && i < papers.size(); i++){
            p[i] = papers.get(i);
        }

        /*for (int i= 0; i < p.length; i++){
            System.out.println( i+ " " +p[i] );
        }*/
        //System.out.println(p);
        return p;
    }


    /**
     * Retrieves a list of papers that have not been downloaded or viewed.
     *
     * Time complexity: O(N) N = number of papers inside HashMap
     *<p>
     * This method traverses the `mapDOI` HashMap, which maps DOI (Digital Object Identifier) strings
     * to `Paper` objects. It checks each paper to see if it has zero downloads and zero views.
     * If both conditions are met, the paper is added to the returned list.
     *</p>
     * @return an {@code ArrayList} of {@code Paper} objects that have not been downloaded or viewed
     */
    public ArrayList<Paper> papersNotDownloadedNotViewed() {
        ArrayList<Paper> papersFound = new ArrayList<>();

        //traverse HashMap Key: DOI -> Val : Paper and check if they don't have any view or download add to ArrayList
        for (String curr : mapDOI.keySet()){
            Paper p = mapDOI.get(curr);
            if (p.getNumDownloads() == 0 && p.getTotalNumViews() == 0){
                papersFound.add(p);
            }
        }

      return papersFound;
    }





    public static void main(String[] args) {
        DataBase db = new DataBase();

        Date bdate = new Date(12,10,2003);
        Date bdate1 = new Date(10,10,1290);


        Long id1 = 1L;
        Author a1 = new Author(null,bdate,"Joel", "Rua dos Pombos", "JJ","Porto Editora", "19","19","joelgamesxd","joelgames23");
        Paper p1 = new Paper();
        Author a2 = new Author(null,bdate,"Joel", "Rua dos Pombos", "JJ","Porto Editora", "19","19","joelgamesxd","joelgames23");

        p1.setDate(bdate);
        p1.setTitle("A historia de Joel, o Homem!");
        Paper p2 = new PaperConference();
        p2.setDate(bdate1);
        p2.setTitle("A historia de Joelzinho, o Rapaz!");
        p2.setDate(bdate1);
        p1.setDate(bdate);
        a1.addPaper(p2);
        a1.addPaper(p1);
        p1.addView();
        p2.addView();

        p2.addView();


        db.insert(a1);
        db.insert(a2);
        db.insert(p1);
        db.insert(p2);

        /*
        Paper test = db.getPaperTest(p1.getDoi());

        System.out.println("Papers:");
        System.out.println(test);
        System.out.println("\n");
        */

       /* ArrayList<Paper> testPaperNoViewsNoDownloads =  db.papersNotDownloadedNotViewed();

        System.out.println(testPaperNoViewsNoDownloads);

       //ArrayList<String> a =   db.paperAuthorByIdPeriodIn(id1, LocalDate.of(999, 10, 21), LocalDate.now());
       ArrayList<String> testAuthorName = db.paperAuthorByNamePeriod("Joel", LocalDate.of(800, 10, 21), LocalDate.now());

       System.out.println(testAuthorName);*/

      // ArrayList<Paper> pex = db.top3PapersMostDownloads();

       //System.out.println(pex);

       //db.listAuthors();
        Object o = new Object();
        o = new DataBase<>();
       db.listPapers();
    }
}
