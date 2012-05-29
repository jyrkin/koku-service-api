/*
 * Copyright 2012 Ixonos Plc, Finland. All rights reserved.
 * 
 * This file is part of Kohti kumppanuutta.
 *
 * This file is licensed under GNU LGPL version 3.
 * Please see the 'license.txt' file in the root directory of the package you received.
 * If you did not receive a license, please contact the copyright holder
 * (kohtikumppanuutta@ixonos.com).
 *
 */
package fi.koku.services.entity.person.v1;

import java.util.List;

/**
 * Class that holds Group related information
 * 
 * @author tuomape
 *
 */
public class Group {
  
  private String id;
  
  private List<Person> persons;
  

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<Person> getPersons() {
    return persons;
  }

  public void setPersons(List<Person> persons) {
    this.persons = persons;
  }
  
  public void addPerson( Person person ) {
    this.persons.add(person);
  }

}
