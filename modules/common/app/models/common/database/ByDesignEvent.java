/*
 * ---------------------------------
 * Copyright (c) 2018
 * RESOLVE Software Research Group
 * School of Computing
 * Clemson University
 * All rights reserved.
 * ---------------------------------
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */

package models.common.database;

import java.util.Date;
import java.util.List;
import javax.persistence.*;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import play.data.validation.Constraints;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;

/**
 * <p>This class is the relational mapping of a user event in the database and provides
 * methods to change the {@code byDesign} events in the database.</p>
 *
 * @author Yu-Shan Sun
 * @version 1.0
 */
@Entity
@Table(name = "byDesignEvents")
public class ByDesignEvent {

    // ===========================================================
    // Global Variables
    // ===========================================================

    /** <p>Unique ID for each {@code byDesign} event.</p> */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /** <p>Author ID associated with this {@code byDesign} event.</p> */
    @Constraints.Required
    public long author;

    /** <p>Code associated with this {@code byDesign} event.</p> */
    @Lob
    public String code;

    /**
     * <p>Boolean flag that indicates whether or not the code referred by this
     * {@code byDesign} event verified.</p>
     */
    @Constraints.Required
    public boolean correct;

    /** <p>Lesson name associated with this {@code byDesign} event.</p> */
    public String lesson;

    /** <p>Module name associated with this {@code byDesign} event.</p> */
    public String module;

    /** <p>Points associated with this {@code byDesign} event.</p> */
    @Constraints.Required
    public long points;

    /** <p>Time spent on the code associated with this {@code byDesign} event.</p> */
    @Constraints.Required
    public long time;

    /** <p>Date associated with this {@code byDesign} event.</p> */
    @Column(name = "eventDate", columnDefinition = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    public Date eventDate;

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * <p>Default constructor. JPA needs this on some occasions.</p>
     */
    private ByDesignEvent() {}

    /**
     * <p>Creates a compiler related {@code byDesign} event object.</p>
     *
     * @param bdAuthor The author's id number.
     * @param bdCode The code associated with this event.
     * @param bdCorrect A flag that indicates whether the author got this lesson
     *                  correctly or not.
     * @param bdLesson The lesson associated with this event.
     * @param bdModule The module associated with this event.
     * @param bdPoints The amount of points earned by the author.
     * @param bdTime The time spent on this lesson.
     */
    private ByDesignEvent(long bdAuthor, String bdCode, boolean bdCorrect,
            String bdLesson, String bdModule, long bdPoints, long bdTime) {
        author = bdAuthor;
        code = bdCode;
        correct = bdCorrect;
        lesson = bdLesson;
        module = bdModule;
        points = bdPoints;
        time = bdTime;
        eventDate = new Date();
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /**
     * <p>Add a {@code byDesign} related event to the database.</p>
     *
     * @param bdAuthor The author's id number.
     * @param bdCode The code associated with this event.
     * @param bdCorrect A flag that indicates whether the author got this lesson
     *                  correctly or not.
     * @param bdLesson The lesson associated with this event.
     * @param bdModule The module associated with this event.
     * @param bdPoints The amount of points earned by the author.
     * @param bdTime The time spent on this lesson.
     *
     * @return The newly created {@code byDesign} event object.
     */
    @Transactional
    public static ByDesignEvent addByDesignEvent(long bdAuthor, String bdCode,
            boolean bdCorrect, String bdLesson, String bdModule, long bdPoints,
            long bdTime) {
        ByDesignEvent bde =
                new ByDesignEvent(bdAuthor, bdCode, bdCorrect, bdLesson,
                        bdModule, bdPoints, bdTime);
        bde.save();

        return bde;
    }

    /**
     * <p>Retrieves the code that stored in the specified event ID.</p>
     *
     * @param id The {@code byDesign} event ID.
     *
     * @return The code as a string.
     */
    @Transactional(readOnly = true)
    public static String getUserEventCode(Long id) {
        Query query =
                JPA.em()
                        .createQuery(
                                "select bde.code from ByDesignEvent bde where bde.id = :id",
                                String.class);
        query.setParameter("id", id);
        query.setMaxResults(1);

        return (String) query.getSingleResult();
    }

    /**
     * <p>Retrieves the list of events generated by the specified author.</p>
     *
     * @param authorID An author ID.
     *
     * @return List of all {@code byDesign} events generated by
     * the specified author ID.
     */
    @Transactional(readOnly = true)
    public static List<ByDesignEvent> getUserEvents(Long authorID) {
        Query query =
                JPA.em().createQuery(
                        "from ByDesignEvent bde where bde.author = :author",
                        ByDesignEvent.class);
        query.setParameter("author", authorID);
        List results = query.getResultList();

        return Lists.newArrayList(Iterables
                .filter(results, ByDesignEvent.class));
    }

    // ===========================================================
    // Private Methods
    // ===========================================================

    /**
     * <p>Store this {@code byDesign} event information.</p>
     */
    @Transactional
    private void save() {
        JPA.em().persist(this);
    }

}