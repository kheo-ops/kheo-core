Feature: Manage servers
    
    Scenario: As a user, I can add a connectable server
        Given A server "localhost" with access enabled to "root" with "password" on port "22222"
        When I add a "localhost" server with user "root" and password "password" on port "22222"
        Then I can retrieve the server "localhost"
        And SSH connectivity is "false"
        And state is "REGISTERED"
        And user is "root"
        And password is "password"
        And port is "22222"

    Scenario: As a user, I can add a non connectable server
        Given A server "non_connectable_server" with access disabled to "root" with "password" on port "22"
        When I add a "non_connectable_server" server with user "root" and password "password" on port "22"
        Then I can retrieve the server "non_connectable_server"
        And state is "REGISTERED"
        And user is "root"
        And password is "password"
        And port is "22"

    Scenario: As a user, I can remove an existing server
        Given An existing server "server_to_remove"
        When I remove "server_to_remove"
        And I retrieve the server "server_to_remove"
        Then "server_to_remove" is not found

    Scenario: As a user, I cannot remove a non existing server
        Given A non existing server "server_to_remove"
        When I remove "server_to_remove"
        Then "server_to_remove" is not found

    Scenario: As a user, I can update an existing server
        Given An existing server "server_to_update"
        When I update "server_to_update" with user "test_user" and password "test_password"
        Then I retrieve the server "server_to_update"
        And "server_to_update" user is "test_user"
        And password is "test_password"

    Scenario: As a user, I cannot update a non existing server
        Given A non existing server "server_to_update_2"
        When I update "server_to_update_2" with user "test_user" and password "test_password"
        Then "server_to_update_2" is not found

