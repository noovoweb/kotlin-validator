package com.noovoweb.validator.integration

import com.noovoweb.validator.Alpha
import com.noovoweb.validator.Email
import com.noovoweb.validator.MinLength
import com.noovoweb.validator.Required
import com.noovoweb.validator.Valid
import com.noovoweb.validator.Validated
import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Validated
data class Member(
    @Required
    @Alpha
    val name: String?,
    @Required
    @Email
    val email: String?,
)

@Validated
data class Team(
    @Required
    @MinLength(3)
    val teamName: String?,
    @Valid(each = true)
    val members: List<Member>?,
)

@Validated
data class Department(
    @Required
    @MinLength(3)
    val departmentName: String?,
    @Valid(each = true)
    val teams: List<Team>?,
)

@Validated
data class Organization(
    @Required
    val organizationName: String?,
    @Valid(each = true)
    val departments: List<Department>?,
)

class NestedArrayValidationIntegrationTest {
    @Test
    fun `should validate deeply nested arrays with correct error paths`() =
        runTest {
            val validator = OrganizationValidator()

            val organization =
                Organization(
                    organizationName = "TechCorp",
                    departments =
                        listOf(
                            Department(
                                departmentName = "Engineering",
                                teams =
                                    listOf(
                                        Team(
                                            teamName = "Backend",
                                            members =
                                                listOf(
                                                    Member(name = "Alice", email = "invalid-email"), // Invalid email
                                                    Member(name = "Bob", email = "bob@example.com"),
                                                ),
                                        ),
                                        Team(
                                            teamName = "UI", // Too short (min 3)
                                            members =
                                                listOf(
                                                    Member(name = "Charlie", email = "charlie@example.com"),
                                                ),
                                        ),
                                    ),
                            ),
                            Department(
                                departmentName = "Sales",
                                teams =
                                    listOf(
                                        Team(
                                            teamName = "North",
                                            members =
                                                listOf(
                                                    Member(name = "David", email = "david@example.com"),
                                                    Member(name = "Eve", email = "eve@example.com"),
                                                    Member(name = "Frank123", email = "frank@example.com"), // Invalid name (not alpha)
                                                ),
                                        ),
                                    ),
                            ),
                        ),
                )

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(organization)
                }

            // Verify specific error paths
            assertTrue(exception.errors.containsKey("departments[0].teams[0].members[0].email"))
            assertTrue(exception.errors.containsKey("departments[0].teams[1].teamName"))
            assertTrue(exception.errors.containsKey("departments[1].teams[0].members[2].name"))

            // Verify error messages exist
            assertTrue(exception.errors["departments[0].teams[0].members[0].email"]!!.isNotEmpty())
            assertTrue(exception.errors["departments[0].teams[1].teamName"]!!.isNotEmpty())
            assertTrue(exception.errors["departments[1].teams[0].members[2].name"]!!.isNotEmpty())
        }

    @Test
    fun `should validate valid deeply nested arrays`() =
        runTest {
            val validator = OrganizationValidator()

            val organization =
                Organization(
                    organizationName = "TechCorp",
                    departments =
                        listOf(
                            Department(
                                departmentName = "Engineering",
                                teams =
                                    listOf(
                                        Team(
                                            teamName = "Backend",
                                            members =
                                                listOf(
                                                    Member(name = "Alice", email = "alice@example.com"),
                                                    Member(name = "Bob", email = "bob@example.com"),
                                                ),
                                        ),
                                    ),
                            ),
                        ),
                )

            validator.validate(organization)
        }

    @Test
    fun `should collect multiple errors across nested arrays`() =
        runTest {
            val validator = OrganizationValidator()

            val organization =
                Organization(
                    organizationName = "TechCorp",
                    departments =
                        listOf(
                            Department(
                                departmentName = "Engineering",
                                teams =
                                    listOf(
                                        Team(
                                            teamName = "AB", // Too short
                                            members =
                                                listOf(
                                                    Member(name = "Alice123", email = "invalid"), // Both invalid
                                                    Member(name = "Bob", email = "also-invalid"), // Invalid email
                                                ),
                                        ),
                                        Team(
                                            teamName = "XY", // Too short
                                            members =
                                                listOf(
                                                    Member(name = "Charlie@", email = "charlie@example.com"), // Invalid name
                                                ),
                                        ),
                                    ),
                            ),
                            Department(
                                departmentName = "HR",
                                teams =
                                    listOf(
                                        Team(
                                            teamName = "Recruitment",
                                            members =
                                                listOf(
                                                    Member(name = "David", email = "not-an-email"), // Invalid email
                                                    Member(name = "123Eve", email = "eve@example.com"), // Invalid name
                                                ),
                                        ),
                                    ),
                            ),
                        ),
                )

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(organization)
                }

            // Should have multiple errors
            assertTrue(exception.errors.size >= 8)

            // Verify some specific paths
            assertTrue(exception.errors.containsKey("departments[0].teams[0].teamName"))
            assertTrue(exception.errors.containsKey("departments[0].teams[0].members[0].name"))
            assertTrue(exception.errors.containsKey("departments[0].teams[0].members[0].email"))
            assertTrue(exception.errors.containsKey("departments[0].teams[0].members[1].email"))
            assertTrue(exception.errors.containsKey("departments[0].teams[1].teamName"))
            assertTrue(exception.errors.containsKey("departments[0].teams[1].members[0].name"))
            assertTrue(exception.errors.containsKey("departments[1].teams[0].members[0].email"))
            assertTrue(exception.errors.containsKey("departments[1].teams[0].members[1].name"))
        }

    @Test
    fun `should handle empty nested arrays`() =
        runTest {
            val validator = OrganizationValidator()

            val organization =
                Organization(
                    organizationName = "TechCorp",
                    departments =
                        listOf(
                            Department(
                                departmentName = "Engineering",
                                teams = emptyList(),
                            ),
                        ),
                )

            validator.validate(organization)
        }

    @Test
    fun `should handle null nested arrays`() =
        runTest {
            val validator = OrganizationValidator()

            val organization =
                Organization(
                    organizationName = "TechCorp",
                    departments =
                        listOf(
                            Department(
                                departmentName = "Engineering",
                                teams = null,
                            ),
                        ),
                )

            validator.validate(organization)
        }

    @Test
    fun `should provide correct index in error path for middle element`() =
        runTest {
            val validator = OrganizationValidator()

            val organization =
                Organization(
                    organizationName = "TechCorp",
                    departments =
                        listOf(
                            Department(
                                departmentName = "Engineering",
                                teams =
                                    listOf(
                                        Team(
                                            teamName = "Backend",
                                            members =
                                                listOf(
                                                    Member(name = "Alice", email = "alice@example.com"),
                                                    Member(name = "Bob", email = "bob@example.com"),
                                                    Member(name = "Charlie123", email = "charlie@example.com"), // Index 2, invalid name
                                                    Member(name = "David", email = "david@example.com"),
                                                ),
                                        ),
                                    ),
                            ),
                        ),
                )

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(organization)
                }

            assertTrue(exception.errors.containsKey("departments[0].teams[0].members[2].name"))
            assertEquals(1, exception.errors.size)
        }

    @Test
    fun `should validate multiple departments with mixed valid and invalid data`() =
        runTest {
            val validator = OrganizationValidator()

            val organization =
                Organization(
                    organizationName = "TechCorp",
                    departments =
                        listOf(
                            Department(
                                departmentName = "Engineering",
                                teams =
                                    listOf(
                                        Team(
                                            teamName = "Backend",
                                            members =
                                                listOf(
                                                    Member(name = "Alice", email = "alice@example.com"), // Valid
                                                ),
                                        ),
                                    ),
                            ),
                            Department(
                                departmentName = "Sales",
                                teams =
                                    listOf(
                                        Team(
                                            teamName = "North",
                                            members =
                                                listOf(
                                                    Member(name = "Bob", email = "invalid-email"), // Invalid at index [1]
                                                ),
                                        ),
                                    ),
                            ),
                        ),
                )

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(organization)
                }

            assertTrue(exception.errors.containsKey("departments[1].teams[0].members[0].email"))
            assertEquals(1, exception.errors.size)
        }
}
